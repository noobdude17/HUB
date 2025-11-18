const AuthAPI = {
    login: "/api/auth/login",
    register: "/api/auth/register",
    forgot: "/api/auth/forgot-password",
    reset: "/api/auth/reset-password"
};

const ProfileAPI = {
    detail: (userId) => `/api/profile/${userId}`,
    update: (userId) => `/api/profile/${userId}`,
    changePassword: (userId) => `/api/profile/${userId}/change-password`
};

const PASSWORD_RULE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).{8,}$/;
const USER_STORAGE_KEY = "cinemaHubUser";
let modalOverlayRef = null;
const modalMap = {};
let userMenuInitialized = false;
let profileToastTimeout = null;

document.addEventListener("DOMContentLoaded", () => {
    initModals();
    bindLoginForm();
    bindRegisterForm();
    bindForgotForm();
    bindResetForm();
    initializeUserState();
    initializeProfilePage();
});

function initModals() {
    modalOverlayRef = document.getElementById("modalOverlay");
    modalMap.login = document.getElementById("loginModal");
    modalMap.register = document.getElementById("registerModal");

    if (!modalOverlayRef) {
        return;
    }

    document.getElementById("openLoginModal")?.addEventListener("click", () => showModal("login"));
    document.getElementById("openRegisterModal")?.addEventListener("click", () => showModal("register"));

    modalOverlayRef.addEventListener("click", (event) => {
        if (event.target === modalOverlayRef) {
            hideModals();
        }
    });

    document.querySelectorAll("[data-modal-close]").forEach((btn) =>
        btn.addEventListener("click", hideModals)
    );

    document.querySelectorAll("[data-open-modal]").forEach((btn) => {
        const target = btn.getAttribute("data-open-modal");
        btn.addEventListener("click", () => showModal(target));
    });
}

function showModal(name) {
    if (!modalOverlayRef || !modalMap[name]) return;
    Object.values(modalMap).forEach((modal) => modal?.classList.remove("show"));
    modalMap[name].classList.add("show");
    modalOverlayRef.classList.add("show");
}

function hideModals() {
    modalOverlayRef?.classList.remove("show");
    Object.values(modalMap).forEach((modal) => modal?.classList.remove("show"));
}

function getStoredUser() {
    try {
        const raw = localStorage.getItem(USER_STORAGE_KEY);
        return raw ? JSON.parse(raw) : null;
    } catch {
        return null;
    }
}

function setStoredUser(user) {
    localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
}

function clearStoredUser() {
    localStorage.removeItem(USER_STORAGE_KEY);
}

function initializeUserState() {
    const user = getStoredUser();
    const authButtons = document.getElementById("authActionButtons");
    const userMenuWrapper = document.getElementById("userMenuWrapper");
    const userNameLabel = document.getElementById("userMenuName");

    if (authButtons) {
        authButtons.classList.toggle("hidden", !!user);
    }
    if (userMenuWrapper && userNameLabel) {
        const displayName = user?.fullName || user?.email || "TÃ i khoáº£n";
        userNameLabel.textContent = displayName;
        userMenuWrapper.style.display = user ? "flex" : "none";
    }

    setupUserMenuHandlers();
}

function setupUserMenuHandlers() {
    const toggle = document.getElementById("userMenuToggle");
    const dropdown = document.getElementById("userMenuDropdown");
    const wrapper = document.getElementById("userMenuWrapper");

    if (!toggle || !dropdown || !wrapper || userMenuInitialized) {
        return;
    }
    userMenuInitialized = true;

    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        dropdown.classList.toggle("show");
        wrapper.classList.toggle("open");
    });

    document.addEventListener("click", (event) => {
        if (!wrapper.contains(event.target)) {
            dropdown.classList.remove("show");
            wrapper.classList.remove("open");
        }
    });

    dropdown.querySelectorAll("button[data-user-action]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const action = btn.getAttribute("data-user-action");
            handleUserMenuAction(action);
            dropdown.classList.remove("show");
            wrapper.classList.remove("open");
        });
    });
}

function handleUserMenuAction(action) {
    if (action === "profile") {
        window.location.href = "/profile";
        return;
    }
    if (action === "logout") {
        clearStoredUser();
        initializeUserState();
        hideModals();
        window.location.href = "/";
    }
}

function bindLoginForm() {
    const form = document.getElementById("loginForm");
    const messageEl = document.getElementById("loginMessage");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const payload = {
            email: document.getElementById("loginEmail").value.trim(),
            password: document.getElementById("loginPassword").value
        };
        await handleRequest(AuthAPI.login, payload, messageEl, (data) => {
            setStoredUser({
                userId: data.userId,
                fullName: data.fullName,
                email: data.email,
                role: data.role,
                phone: data.phone || ""
            });
            initializeUserState();
            hideModals();
            const role = (data.role || "").toUpperCase();
            if (role === "ADMIN") {
                window.location.href = "/admin/dashboard";
            } else if (role === "STAFF" || role === "EMPLOYEE") {
                window.location.href = "/staff/portal";
            } else {
                window.location.href = "/";
            }
        });
    });
}

function bindRegisterForm() {
    const form = document.getElementById("registerForm");
    const messageEl = document.getElementById("registerMessage");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const firstName = document.getElementById("registerFirstName")?.value.trim() ?? "";
        const lastName = document.getElementById("registerLastName")?.value.trim() ?? "";
        let fullName = document.getElementById("registerFullName")?.value.trim() ?? "";
        if (!fullName) {
            fullName = `${firstName} ${lastName}`.trim();
        }
        const password = document.getElementById("registerPassword").value;
        const confirmPassword = document.getElementById("registerConfirmPassword").value;

        if (!validatePasswordStrength(password)) {
            setFormMessage(messageEl, "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt, tối thiểu 8 ký tự.");
            return;
        }
        if (password !== confirmPassword) {
            setFormMessage(messageEl, "Mật khẩu xác nhận không khớp.");
            return;
        }

        const payload = {
            fullName,
            email: document.getElementById("registerEmail").value.trim(),
            phone: document.getElementById("registerPhone").value.trim(),
            password,
            confirmPassword
        };
        await handleRequest(AuthAPI.register, payload, messageEl, () => {
            setFormMessage(messageEl, "Đăng ký thành công! Vui lòng đăng nhập.", "success");
            setTimeout(() => {
                hideModals();
                showModal("login");
            }, 1400);
        });
    });
}

function bindForgotForm() {
    const emailForm = document.getElementById("forgotPasswordForm");
    const verifyForm = document.getElementById("forgotVerificationForm");
    const messageEl = document.getElementById("forgotMessage");
    const stepEmail = document.getElementById("forgotStepEmail");
    const stepCode = document.getElementById("forgotStepCode");
    const emailDisplay = document.getElementById("forgotEmailDisplay");
    const emailHidden = document.getElementById("forgotEmailHidden");
    if (!emailForm) return;

    let cachedEmail = "";

    emailForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        cachedEmail = document.getElementById("forgotEmail").value.trim();
        if (!cachedEmail) {
            setFormMessage(messageEl, "Vui lòng nhập email đăng ký.");
            return;
        }
        await handleRequest(AuthAPI.forgot, { email: cachedEmail }, messageEl, () => {
            if (emailHidden) {
                emailHidden.value = cachedEmail;
            }
            if (emailDisplay) {
                emailDisplay.textContent = cachedEmail;
            }
            stepEmail?.classList.add("hidden");
            stepCode?.classList.remove("hidden");
            setFormMessage(messageEl, "Đã gửi mã xác thực, vui lòng kiểm tra email.", "success");
        });
    });

    if (verifyForm) {
        verifyForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = cachedEmail || emailHidden?.value || "";
            const token = document.getElementById("forgotToken").value.trim();
            const newPassword = document.getElementById("forgotNewPassword").value;
            const confirmPassword = document.getElementById("forgotConfirmPassword").value;

            if (!email) {
                setFormMessage(messageEl, "Vui lòng nhập email trước khi đổi mật khẩu.");
                return;
            }
            if (!token) {
                setFormMessage(messageEl, "Vui lòng nhập mã xác thực.");
                return;
            }
            if (!validatePasswordStrength(newPassword)) {
                setFormMessage(messageEl, "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt, tối thiểu 8 ký tự.");
                return;
            }
            if (newPassword !== confirmPassword) {
                setFormMessage(messageEl, "Mật khẩu xác nhận không khớp.");
                return;
            }

            const payload = {
                email,
                token,
                newPassword,
                confirmPassword
            };
            await handleRequest(AuthAPI.reset, payload, messageEl, () => {
                setFormMessage(messageEl, "Đổi mật khẩu thành công! Đang chuyển về trang đăng nhập...", "success");
                setTimeout(() => window.location.href = "/login", 1500);
            });
        });
    }
}

function bindResetForm() {
    const form = document.getElementById("resetPasswordForm");
    const messageEl = document.getElementById("resetMessage");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const newPassword = document.getElementById("resetPassword").value;
        const confirmPassword = document.getElementById("resetConfirmPassword").value;

        if (!validatePasswordStrength(newPassword)) {
            setFormMessage(messageEl, "Mật khẩu phải có chữ hoa, chữ thường, số và ký tự đặc biệt, tối thiểu 8 ký tự.");
            return;
        }
        if (newPassword !== confirmPassword) {
            setFormMessage(messageEl, "Mật khẩu xác nhận không khớp.");
            return;
        }

        const payload = {
            email: document.getElementById("resetEmail").value.trim(),
            token: document.getElementById("resetToken").value.trim(),
            newPassword,
            confirmPassword
        };
        await handleRequest(AuthAPI.reset, payload, messageEl, () => {
            setFormMessage(messageEl, "Đổi mật khẩu thành công! Đang chuyển về trang đăng nhập...", "success");
            setTimeout(() => window.location.href = "/login", 1500);
        });
    });
}

function initializeProfilePage() {
    const profileTabs = document.getElementById("profileTabs");
    if (!profileTabs) {
        return;
    }

    const user = getStoredUser();
    if (!user) {
        window.location.href = "/";
        return;
    }

    loadProfileData(user);

    profileTabs.querySelectorAll(".tab-button").forEach((btn) => {
        btn.addEventListener("click", () => switchProfileTab(btn.getAttribute("data-tab-target")));
    });

    const profileForm = document.getElementById("profileForm");
    if (profileForm) {
        profileForm.addEventListener("submit", async (event) => {
            event.preventDefault();
            const payload = {
                firstName: document.getElementById("profileFirstName").value.trim(),
                lastName: document.getElementById("profileLastName").value.trim(),
                phone: document.getElementById("profilePhone").value.trim()
            };
            try {
                const updated = await apiRequest(ProfileAPI.update(user.userId), {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(payload)
                });
                showProfileToast("Lưu thành công !");
                const fullName = buildFullName(payload.firstName, payload.lastName) || user.fullName;
                const syncedUser = {
                    ...user,
                    fullName,
                    phone: updated.phone
                };
                setStoredUser(syncedUser);
                Object.assign(user, syncedUser);
                initializeUserState();
            } catch (error) {
                showProfileToast(error.message, "error");
            }
        });
    }

    setupChangePasswordModal(user);

    renderTicketHistory(user.ticketHistory || []);
}

function switchProfileTab(targetId) {
    document.querySelectorAll(".profile-section").forEach((section) => {
        section.classList.toggle("active", section.id === targetId);
    });
    document.querySelectorAll(".tab-button").forEach((btn) => {
        btn.classList.toggle("active", btn.getAttribute("data-tab-target") === targetId);
    });
}

function renderTicketHistory(entries) {
    const tbody = document.getElementById("ticketHistoryBody");
    if (!tbody) return;
    tbody.innerHTML = "";
    if (!entries.length) {
        const row = document.createElement("tr");
        const col = document.createElement("td");
        col.colSpan = 4;
        col.className = "text-center text-muted";
        col.textContent = "Chưa có dữ liệu";
        row.appendChild(col);
        tbody.appendChild(row);
        return;
    }
    entries.forEach((item) => {
        const row = document.createElement("tr");
        ["date", "movie", "tickets", "amount"].forEach((field) => {
            const cell = document.createElement("td");
            cell.textContent = item[field] ?? "";
            row.appendChild(cell);
        });
        tbody.appendChild(row);
    });
}

async function loadProfileData(user) {
    try {
        const profile = await apiRequest(ProfileAPI.detail(user.userId));
        fillProfileForm(profile, user);
        const fullName = buildFullName(profile.firstName, profile.lastName);
        const syncedUser = {
            ...user,
            fullName: fullName || user.fullName,
            phone: profile.phone
        };
        setStoredUser(syncedUser);
        Object.assign(user, syncedUser);
        initializeUserState();
    } catch (error) {
        showProfileToast(error.message || "Không thể tải thông tin tài khoản", "error");
    }
}

function fillProfileForm(profile, user) {
    document.getElementById("profileFirstName").value = profile.firstName || "";
    document.getElementById("profileLastName").value = profile.lastName || "";
    document.getElementById("profilePhone").value = profile.phone || "";
    document.getElementById("profileUsername").value = user.email?.split("@")[0] || "";
    document.getElementById("profileEmail").value = user.email || "";
}

function setupChangePasswordModal(user) {
    const openBtn = document.getElementById("changePasswordBtn");
    const closeBtn = document.getElementById("closeChangePasswordModal");
    const cancelBtn = document.getElementById("cancelChangePassword");
    const modal = document.getElementById("changePasswordModal");
    const form = document.getElementById("changePasswordForm");

    if (!openBtn || !modal || !form) {
        return;
    }

    const closeModal = () => toggleChangePasswordModal(false);

    openBtn.addEventListener("click", () => toggleChangePasswordModal(true));
    closeBtn?.addEventListener("click", closeModal);
    cancelBtn?.addEventListener("click", closeModal);
    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const payload = {
            currentPassword: document.getElementById("currentPassword").value,
            newPassword: document.getElementById("newPassword").value,
            confirmPassword: document.getElementById("confirmPassword").value
        };
        try {
            await apiRequest(ProfileAPI.changePassword(user.userId), {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(payload)
            });
            showProfileToast("Lưu thành công !");
            form.reset();
            closeModal();
        } catch (error) {
            showProfileToast(error.message, "error");
        }
    });
}

function toggleChangePasswordModal(show) {
    const modal = document.getElementById("changePasswordModal");
    if (!modal) return;
    modal.classList.toggle("show", show);
    modal.setAttribute("aria-hidden", show ? "false" : "true");
    if (!show) {
        document.getElementById("changePasswordForm")?.reset();
    }
}

function showProfileToast(message, variant = "success") {
    const toast = document.getElementById("profileToast");
    if (!toast) return;
    toast.textContent = message;
    toast.classList.remove("error");
    if (variant === "error") {
        toast.classList.add("error");
    }
    toast.classList.add("show");
    clearTimeout(profileToastTimeout);
    profileToastTimeout = setTimeout(() => toast.classList.remove("show"), 3200);
}

function buildFullName(firstName, lastName) {
    return [firstName, lastName]
        .map((part) => (part || "").trim())
        .filter(Boolean)
        .join(" ")
        .trim();
}

function validatePasswordStrength(password) {
    return PASSWORD_RULE.test(password);
}

function setFormMessage(element, text, variant) {
    if (!element) return;
    element.textContent = text;
    element.classList.remove("success", "error");
    if (variant === "success" || variant === "error") {
        element.classList.add(variant);
    }
}

async function handleRequest(url, payload, messageEl, onSuccess) {
    try {
        setFormMessage(messageEl, "Đang xử lý...");
        const data = await apiRequest(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });
        if (typeof onSuccess === "function") {
            onSuccess(data);
        }
    } catch (error) {
        console.error(error);
        setFormMessage(messageEl, error.message, "error");
    }
}

async function apiRequest(url, options = {}) {
    const response = await fetch(url, options);
    let data = {};
    const rawText = await response.text();
    if (rawText) {
        try {
            data = JSON.parse(rawText);
        } catch (err) {
            console.warn("Response is not JSON", err);
        }
    }
    if (!response.ok) {
        throw new Error(data.message || data.error || "Đã xảy ra lỗi");
    }
    return data;
}








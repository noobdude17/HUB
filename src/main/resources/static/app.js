const MOVIE_API = "http://localhost:8080/api/movies/now-showing";
const PLACEHOLDER_POSTER = "https://via.placeholder.com/320x480.png?text=Cinema+HUB";

document.addEventListener("DOMContentLoaded", () => {
    if (document.getElementById("movieGrid")) {
        fetchNowShowing();
    }
});

async function fetchNowShowing() {
    const grid = document.getElementById("movieGrid");
    const statusMessage = document.getElementById("statusMessage");
    if (!grid) {
        return;
    }

    try {
        const response = await fetch(MOVIE_API);
        if (!response.ok) {
            throw new Error(`Failed to load movies: ${response.status}`);
        }
        const movies = await response.json();
        renderMovies(grid, movies);
    } catch (error) {
        console.error(error);
        if (statusMessage) {
            statusMessage.textContent = "Unable to load movies. Please try again later.";
        }
    }
}

function renderMovies(grid, movies) {
    grid.innerHTML = "";
    if (!movies || movies.length === 0) {
        const empty = document.createElement("div");
        empty.className = "status-message";
        empty.textContent = "No movies are currently showing.";
        grid.appendChild(empty);
        return;
    }

    movies.forEach(movie => {
        const card = document.createElement("article");
        card.className = "movie-card";

        const poster = document.createElement("img");
        poster.src = movie.posterUrl || PLACEHOLDER_POSTER;
        poster.alt = `${movie.title} poster`;
        card.appendChild(poster);

        const content = document.createElement("div");
        content.className = "movie-content";

        const title = document.createElement("h3");
        title.className = "movie-title";
        title.textContent = movie.title || "Untitled";
        content.appendChild(title);

        const rating = document.createElement("div");
        rating.className = "movie-meta";
        rating.textContent = `Age Rating: ${movie.ageRating || "N/A"}`;
        content.appendChild(rating);

        const release = document.createElement("div");
        release.className = "movie-meta";
        release.textContent = `Release Date: ${formatDate(movie.releaseDate)}`;
        content.appendChild(release);

        const actions = document.createElement("div");
        actions.className = "movie-actions";
        const button = document.createElement("button");
        button.className = "btn";
        button.textContent = "View Details";
        button.addEventListener("click", () => handleViewDetails(movie));

        actions.appendChild(button);
        content.appendChild(actions);

        card.appendChild(content);
        grid.appendChild(card);
    });
}

function formatDate(dateStr) {
    if (!dateStr) {
        return "TBD";
    }
    const date = new Date(dateStr);
    if (Number.isNaN(date.getTime())) {
        return dateStr;
    }
    return date.toLocaleDateString(undefined, {
        year: "numeric",
        month: "short",
        day: "numeric"
    });
}

function handleViewDetails(movie) {
    console.log("View details for movie id:", movie.id);
    alert(`Movie: ${movie.title}`);
}

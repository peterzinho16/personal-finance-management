// Initialize Bootstrap tooltips with custom delay
document.addEventListener('DOMContentLoaded', function () {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"], thead > tr > th[title]');
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl, {
            delay: {"show": 100, "hide": 100}
        });
    });
    const arrayAelement = document.querySelectorAll('table > a');
    arrayAelement.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl, {
            delay: {"show": 100, "hide": 100}
        });
    });
});

// Show error toast
function showErrorToast(status, message) {
    const toastContainer = document.querySelector('.toast-container');
    const toastId = `toast-${Date.now()}`;
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-bg-warning border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>HTTP error ${status}</strong>
                    <div class="content-fluid">${message}</div>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 10000  // Duration in milliseconds (10000ms = 10 seconds)
    });
    toast.show();
}

// Show success toast
function showSuccessToast(response) {
    console.log(response);
    const toastContainer = document.querySelector('.toast-container');
    const toastId = `toast-${Date.now()}`;
    const toastHTML = `
        <div id="${toastId}" class="toast align-items-center text-bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>Peticion exitosa!</strong>
                    <div class="content-fluid"></div>
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    toastContainer.insertAdjacentHTML('beforeend', toastHTML);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: 10000  // Duration in milliseconds (10000ms = 10 seconds)
    });
    toast.show();
}

// Toggle sidebar
$(document).ready(function () {
    $("#toggle-sidebar").on("click", function () {
        $("#sidebar").toggleClass("expanded");
        $(".content").toggleClass("expanded");
    });

    // Close slide form and hide save changes button
    $("#closeSlideForm").on("click", function () {
        $("#slideForm").removeClass("active");
        $("#save-changes").addClass("hidden");
    });

    // Close slide form with Escape key
    $(document).on("keydown", function (event) {
        if (event.key === "Escape") {
            $("#slideForm").removeClass("active");
            $("#save-changes").addClass("hidden");
        }
    });
});

function getCategoryNameWithEmoji(categoryName) {
    const categoryEmojiMap = {
        'Movilidad': '🚗',
        'Vestimenta': '👗',
        'Alimentacion': '🍽️',
        'Salidas': '🍻',
        'Obsequios': '🎁',
        'Servicio': '🛠️',
        'Educacion': '🎓',
        'Viaje': '✈️',
        'Salud': '🩺',
        'Hogar': '🏠',
        'Tecnologia': '💻',
        'Por definir': '❓',
        'Compras': '🛒',
        'Cuidado Corporal' : '🧴'
    };

    return categoryEmojiMap[categoryName] ? `${categoryEmojiMap[categoryName]} ${categoryName}` : categoryName;
}

function generateCatAndSubCat(subCategories, colNum) {
    if(colNum === undefined || isNaN(colNum) || colNum <=3) {
        //colNum = 3;
    }
    const categoryContainer = $("#category-container");
    const categories = {};

    // Group subcategories by category
    subCategories.forEach(subCategory => {
        const categoryName = subCategory.category.name;
        if (!categories[categoryName]) {
            categories[categoryName] = [];
        }
        categories[categoryName].push(subCategory);
    });

    // Generate HTML for categories and subcategories
    console.log(categories);
    for (const [categoryName, subCategories] of Object.entries(categories).toSorted()) {
        const categoryDiv = $(`
                    <div class="category col-12 mb-3">
                        <h5>${getCategoryNameWithEmoji(categoryName)}</h5>
                        <div class="row">
                            ${subCategories.map(subCategory => `
                                <div class="col-lg-3 col-md-4 col-xs-6">
                                    <input class="form-check-input" type="radio" name="subCategory" id="subCategory-${subCategory.id}" value="${subCategory.id}">
                                    <label class="form-check-label" for="subCategory-${subCategory.id}">
                                        ${subCategory.name}
                                    </label>
                                </div>
                            `).join('')}
                        </div>
                    </div>
                `);
        categoryContainer.append(categoryDiv);
    }
}



function formatDateWithMonthText(dateString) {
    const date = new Date(dateString);

    const day = String(date.getDate()).padStart(2, "0");
    const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    const month = months[date.getMonth()]; // Retrieve the 3-character month abbreviation
    const year = date.getFullYear();

    let hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, "0");
    const period = hours >= 12 ? "PM" : "AM";

    // Convert to 12-hour format
    hours = hours % 12 || 12;

    return `${day}/${month}/${year} ${String(hours).padStart(2, "0")}:${minutes} ${period}`;
}

function generateDateTimeNowFormatted() {
    const now = new Date();
    const offset = now.getTimezoneOffset() * 60000; // Convert offset to milliseconds
    return new Date(now.getTime() - offset).toISOString().slice(0, 16);
}

(function eventListenerForSubCategoryList(){
    document.querySelector('#category-container').addEventListener('click', (event) => {
        if(event.target.type === 'radio') {
            console.log(`${event.target.parentElement.textContent.trim()}: ${event.target.value}`);
        }
    })
})();
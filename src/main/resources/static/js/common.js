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
        'Compras': '🛒'
    };

    return categoryEmojiMap[categoryName] ? `${categoryEmojiMap[categoryName]} ${categoryName}` : categoryName;
}

function generateCatAndSubCat(subCategories) {

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
    for (const [categoryName, subCategories] of Object.entries(categories)) {
        const categoryDiv = $(`
                    <div class="category col-12 mb-4">
                        <h5>${getCategoryNameWithEmoji(categoryName)}</h5>
                        <div class="row">
                            ${subCategories.map(subCategory => `
                                <div class="col-lg-4 col-md-4 col-xs-6">
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
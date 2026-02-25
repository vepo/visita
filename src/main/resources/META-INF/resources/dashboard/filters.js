function refreshLinks() {
    [...document.querySelectorAll('a')].forEach(e => {
        //add window url params to to the href's params
        const url = new URL(e.href)
        for (let [k, v] of new URLSearchParams(window.location.search).entries()) {
            url.searchParams.set(k, v)
        }
        e.href = url.toString();
    })
}
document.addEventListener('DOMContentLoaded', function () {
    // Function to format date to YYYY-MM-DD for input fields
    function formatDateForInput(dateString) {
        if (!dateString) return '';

        // If it's already in YYYY-MM-DD format, return as is
        if (/^\d{4}-\d{2}-\d{2}$/.test(dateString)) {
            return dateString;
        }

        // Try to parse the date
        const date = new Date(dateString);
        if (isNaN(date.getTime())) return '';

        // Format to YYYY-MM-DD
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    // Get URL parameters and set input values
    const currentUrl = new URL(window.location.href);
    const params = new URLSearchParams(currentUrl.search);

    const startDateParam = params.get('startDate');
    const endDateParam = params.get('endDate');

    const startDateInput = document.getElementById('startDate');
    const endDateInput = document.getElementById('endDate');

    if (startDateParam && startDateInput) {
        startDateInput.value = formatDateForInput(startDateParam);
    }

    if (endDateParam && endDateInput) {
        endDateInput.value = formatDateForInput(endDateParam);
    }
    refreshLinks();

    const previousFnCall = null;
    function delayed(fn) {
        return () => {
            if (previousFnCall) {
                clearTimeout(previousFnCall);
            }
            previousFnCall = setTimeout(() => {
                previousFnCall = null;
                fn();
            }, 500);
        };
    }

    // Function to update min/max constraints
    function updateDateConstraints() {
        const startDate = startDateInput.value;
        const endDate = endDateInput.value;
        console.log('Value changed!', startDate, endDate);

        // Set min attribute of endDate to startDate (if startDate has a value)
        if (startDate) {
            endDateInput.min = startDate;
        } else {
            endDateInput.removeAttribute('min');
        }

        // Set max attribute of startDate to endDate (if endDate has a value)
        if (endDate) {
            startDateInput.max = endDate;
        } else {
            startDateInput.removeAttribute('max');
        }

        // Validate current values
        if (startDate && endDate && startDate > endDate) {
            // If start date is greater than end date, show warning and disable filter button
            const filterButton = document.getElementById('filterButton');
            filterButton.disabled = true;
            filterButton.classList.add('opacity-50', 'cursor-not-allowed');

            // Show warning message (create if doesn't exist)
            let warningMsg = document.getElementById('date-warning');
            if (!warningMsg) {
                warningMsg = document.createElement('p');
                warningMsg.id = 'date-warning';
                warningMsg.className = 'text-red-600 text-sm mt-2';
                warningMsg.textContent = 'Data inicial não pode ser maior que data final';
                endDateInput.closest('.date-filter').appendChild(warningMsg);
            }
        } else {
            // Enable filter button and remove warning
            const filterButton = document.getElementById('filterButton');
            filterButton.disabled = false;
            filterButton.classList.remove('opacity-50', 'cursor-not-allowed');

            const warningMsg = document.getElementById('date-warning');
            if (warningMsg) {
                warningMsg.remove();
            }
        }
    }

    // Add event listeners for date changes
    startDateInput.addEventListener('change', delayed(updateDateConstraints));
    startDateInput.addEventListener('input', delayed(updateDateConstraints));
    endDateInput.addEventListener('change', delayed(updateDateConstraints));
    endDateInput.addEventListener('input', delayed(updateDateConstraints));

    // Initial constraints update
    updateDateConstraints();

    // Filter button click handler
    document.getElementById('filterButton')
        .addEventListener('click', function () {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            console.log('Filtering', startDate, endDate);

            // Get current URL and its parameters
            const currentUrl = new URL(window.location.href);
            const params = new URLSearchParams(currentUrl.search);

            // Update or remove startDate parameter
            if (startDate) {
                params.set('startDate', startDate);
            } else {
                params.delete('startDate');
            }

            // Update or remove endDate parameter
            if (endDate) {
                params.set('endDate', endDate);
            } else {
                params.delete('endDate');
            }

            // Construct new URL
            const newUrl = currentUrl.pathname + (params.toString() ? '?' + params.toString() : '');

            // Navigate to new URL
            window.location.href = newUrl;
        });

    // Optional: Allow Enter key to trigger filter
    document.getElementById('startDate')
        .addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                document.getElementById('filterButton').click();
            }
        });

    document.getElementById('endDate')
        .addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                document.getElementById('filterButton').click();
            }
        });
});
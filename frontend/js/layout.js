document.addEventListener('DOMContentLoaded', () => {
    // Load header
    fetch('/header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('header-container').innerHTML = data;
            displayUserInfo();
        });

    // Load footer
    fetch('/footer.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('footer-container').innerHTML = data;
        });

    function displayUserInfo() {
        const userInfo = document.getElementById('userInfo');
        const userId = localStorage.getItem('userId');
        const isAnonymous = localStorage.getItem('isAnonymous');

        if (userInfo && userId) {
            if (isAnonymous === 'true') {
                userInfo.textContent = `${userId} (Anonymous)`;
            } else {
                userInfo.textContent = `${userId}`;
            }
        }
    }
});

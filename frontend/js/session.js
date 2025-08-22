
document.addEventListener('DOMContentLoaded', () => {
    const isAnonymous = localStorage.getItem('isAnonymous');

    // 비회원 사용자일 경우에만 타이머 로직 실행
    if (isAnonymous === 'true') {
        const expiresAt = localStorage.getItem('expiresAt');
        if (!expiresAt) {
            return;
        }

        const expirationTime = new Date(expiresAt).getTime();
        let timerInterval;

        function updateTimer() {
            const now = new Date().getTime();
            const distance = expirationTime - now;

            const timerElement = document.getElementById('sessionTimer');

            if (distance < 0) {
                clearInterval(timerInterval);
                alert('세션이 만료되었습니다. 다시 로그인해주세요.');
                localStorage.clear();
                window.location.href = '/index.html';
                return;
            }

            const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
            const seconds = Math.floor((distance % (1000 * 60)) / 1000);

            if (timerElement) {
                timerElement.textContent = `남은 시간: ${minutes}분 ${seconds}초`;
            } else {
                // 헤더가 아직 로드되지 않았을 수 있으므로 다시 시도
            }
        }

        // 헤더가 로드된 후 타이머 업데이트 시작
        const headerContainer = document.getElementById('header-container');
        const observer = new MutationObserver((mutationsList, observer) => {
            for(const mutation of mutationsList) {
                if (mutation.type === 'childList') {
                    timerInterval = setInterval(updateTimer, 1000);
                    updateTimer(); // 즉시 한 번 실행
                    observer.disconnect();
                }
            }
        });
        observer.observe(headerContainer, { childList: true });

    } else {
        // 일반 회원이면 타이머 요소를 숨김
        const headerContainer = document.getElementById('header-container');
        const observer = new MutationObserver((mutationsList, observer) => {
            for(const mutation of mutationsList) {
                if (mutation.type === 'childList') {
                    const timerElement = document.getElementById('sessionTimer');
                    if(timerElement) {
                        timerElement.style.display = 'none';
                    }
                    observer.disconnect();
                }
            }
        });
        observer.observe(headerContainer, { childList: true });
    }
});

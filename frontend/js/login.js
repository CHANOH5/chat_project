const loginForm = document.getElementById('loginForm');
const idInput = document.getElementById('id');
const passwordInput = document.getElementById('password');
const idError = document.getElementById('idError');
const passwordError = document.getElementById('passwordError');

// 쿠키 설정 함수
function setCookie(name, value, days) {
    let expires = "";
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}

loginForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const id = idInput.value;
    const password = passwordInput.value;

    let isValid = true;

    // 아이디 유효성 검사
    if (id.trim() === '') {
        idError.textContent = '아이디를 입력해주세요.';
        isValid = false;
    } else {
        idError.textContent = '';
    }

    // 비밀번호 유효성 검사
    if (password.trim() === '') {
        passwordError.textContent = '비밀번호를 입력해주세요.';
        isValid = false;
    } else {
        passwordError.textContent = '';
    }

    if (!isValid) {
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/v1/login', { 
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ id: id, password: password }),
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            // 로그인 성공 시, 입력한 ID를 userId로 저장
            localStorage.setItem('userId', id); 
            window.location.href = 'intro.html';
        } else {
            const errorData = await response.json();
            passwordError.textContent = errorData.message || '아이디 또는 비밀번호가 올바르지 않습니다.';
        }

    } catch (error) {
        console.error('로그인 중 오류 발생:', error);
        passwordError.textContent = '로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
});

// --- 회원가입 모달 로직 ---
const signupModal = document.getElementById('signupModal');
const signupLink = document.getElementById('signupLink');
const closeButton = document.querySelector('.close-button');

signupLink.onclick = function() {
    signupModal.style.display = "block";
}

closeButton.onclick = function() {
    signupModal.style.display = "none";
}

window.onclick = function(event) {
    if (event.target == signupModal) {
        signupModal.style.display = "none";
    }
}

const signupForm = document.getElementById('signupForm');

signupForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const formData = new FormData();
    const formElements = signupForm.elements;

    let isValid = true;
    const requiredFields = ['signupId', 'signupPassword', 'name', 'nickname'];
    requiredFields.forEach(field => {
        const input = formElements[field];
        const errorDiv = document.getElementById(`${field}Error`);
        if (input.value.trim() === '') {
            errorDiv.textContent = `${input.labels[0].textContent.replace('*', '').trim()}(을)를 입력해주세요.`;
            isValid = false;
        } else {
            errorDiv.textContent = '';
        }
    });

    if (!isValid) {
        return;
    }

    // 필수 필드 추가
    formData.append('id', formElements.signupId.value);
    formData.append('password', formElements.signupPassword.value);
    formData.append('name', formElements.name.value);
    formData.append('nickname', formElements.nickname.value);
    
    // 선택적 필드 추가 (값이 있을 경우에만)
    if (formElements.email.value.trim() !== '') {
        formData.append('email', formElements.email.value);
    }
    if (formElements.profileImage.files.length > 0) {
        formData.append('profileImage', formElements.profileImage.files[0]);
    }

    // 고정 값 추가
    formData.append('isAnonymous', 0);
    formData.append('role', 1);
    formData.append('status', 0);

    try {
        const response = await fetch('http://localhost:8080/api/v1/user', {
            method: 'POST',
            body: formData,
        });

        if (response.ok) {
            alert('회원가입이 완료되었습니다. 로그인 해주세요.');
            signupModal.style.display = "none";
        } else {
            const errorData = await response.json();
            const generalError = document.getElementById('signupPasswordError');
            generalError.textContent = errorData.message || '회원가입에 실패했습니다.';
        }

    } catch (error) {
        console.error('회원가입 중 오류 발생:', error);
        const generalError = document.getElementById('signupPasswordError');
        generalError.textContent = '회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
    }
});

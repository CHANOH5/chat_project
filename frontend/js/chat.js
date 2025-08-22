document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/index.html'; // 토큰 없으면 로그인 페이지로
        return;
    }

    const chatRoomListDiv = document.querySelector('.chat-room-list');

    async function loadChatRooms() {
        try {
            const response = await fetch('http://localhost:8080/api/v1/chat_room', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const chatRooms = await response.json();
            chatRoomListDiv.innerHTML = ''; // 기존 목록 비우기
            chatRooms.forEach(room => {
                const roomElement = document.createElement('div');
                roomElement.classList.add('chat-room-item');
                roomElement.innerHTML = `<h3>${room.name}</h3>`;
                roomElement.dataset.roomId = room.id;
                chatRoomListDiv.appendChild(roomElement);
            });

            // 관리자와 1:1 문의 항목 동적 추가
            const adminChatRoomItem = document.createElement('div');
            adminChatRoomItem.classList.add('chat-room-item');
            adminChatRoomItem.innerHTML = `<h3>관리자와 1:1 문의</h3>`;
            adminChatRoomItem.addEventListener('click', createAdminChatRoom);
            chatRoomListDiv.appendChild(adminChatRoomItem);

        } catch (error) {
            console.error('채팅방 목록 로드 중 오류 발생:', error);
            alert('채팅방 목록을 불러오는데 실패했습니다.');
        }
    }

    async function joinChatRoom(roomId) {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/chat_room/${roomId}/join`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            localStorage.setItem('currentRoomId', roomId);
            window.location.href = `/room.html`;
        } catch (error) {
            console.error('채팅방 입장 중 오류 발생:', error);
            alert('채팅방 입장에 실패했습니다.');
        }
    }

    async function createAdminChatRoom() {
        try {
            const response = await fetch('http://localhost:8080/api/v1/chat_room/private/admin', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const result = await response.json();
            alert('관리자와의 1:1 채팅방이 생성되었습니다.');
            // TODO: 생성된 채팅방으로 이동 또는 웹소켓 연결 로직 추가
        } catch (error) {
            console.error('관리자와 1:1 채팅방 생성 중 오류 발생:', error);
            alert('관리자와의 1:1 채팅방 생성에 실패했습니다.');
        }
    }

    chatRoomListDiv.addEventListener('click', (event) => {
        const clickedItem = event.target.closest('.chat-room-item');
        if (clickedItem) {
            const roomId = clickedItem.dataset.roomId;
            if (roomId) {
                joinChatRoom(roomId);
            }
        }
    });

    function setupHeaderButtons() {
        const logoutButton = document.getElementById('logoutButton');
        if (logoutButton) {
            logoutButton.addEventListener('click', () => {
                localStorage.clear();
                window.location.href = '/index.html';
            });
        }
    }

    const headerContainer = document.getElementById('header-container');
    const observer = new MutationObserver((mutationsList, observer) => {
        for(const mutation of mutationsList) {
            if (mutation.type === 'childList') {
                setupHeaderButtons();
                observer.disconnect();
            }
        }
    });
    observer.observe(headerContainer, { childList: true });

    loadChatRooms();
});

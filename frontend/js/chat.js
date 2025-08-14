console.log('chat.js script loaded and running.'); // ADD THIS

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/index.html'; // 토큰 없으면 로그인 페이지로
    }

    const logoutButton = document.getElementById('logoutButton');
    logoutButton.addEventListener('click', () => {
        localStorage.removeItem('token');
        window.location.href = '/index.html';
    });

    

    // 실제 채팅방 목록 로드 및 생성 로직 추가 예정

    const chatRoomListDiv = document.querySelector('.chat-room-list');

    async function loadChatRooms() {
        console.log('loadChatRooms() called.'); // ADD THIS
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
            console.log('Chat rooms fetched:', chatRooms); // ADD THIS
            chatRoomListDiv.innerHTML = ''; // 기존 목록 비우기
            console.log('Chat room list div cleared.'); // ADD THIS
            chatRooms.forEach(room => {
                console.log('Creating room element for:', room.name); // ADD THIS
                const roomElement = document.createElement('div');
                roomElement.classList.add('chat-room-item');
                roomElement.innerHTML = `
                    <h3>${room.name}</h3>
                `;
                roomElement.dataset.roomId = room.id; // Store roomId on the element itself
                chatRoomListDiv.appendChild(roomElement);
                console.log('Room element appended:', room.name);
            });

            // Add event delegation to the parent container
            chatRoomListDiv.addEventListener('click', (event) => {
                const clickedItem = event.target.closest('.chat-room-item');
                if (clickedItem) {
                    const roomId = clickedItem.dataset.roomId;
                    console.log('Delegated click: Chat room item clicked. Attempting to join roomId:', roomId);
                    joinChatRoom(roomId);
                }
            });

            // 관리자와 1:1 문의 항목 동적 추가
            console.log('Adding Admin Chat Room item.'); // ADD THIS
            const adminChatRoomItem = document.createElement('div');
            adminChatRoomItem.classList.add('chat-room-item');
            adminChatRoomItem.innerHTML = `
                <h3>관리자와 1:1 문의</h3>
            `;
            adminChatRoomItem.addEventListener('click', createAdminChatRoom); // 클릭 시 API 호출
            chatRoomListDiv.appendChild(adminChatRoomItem);
            console.log('Admin Chat Room item appended.'); // ADD THIS

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

            // Only attempt to parse JSON if there's content (not 204 No Content)
            if (response.status !== 204) {
                const result = await response.json();
                console.log('채팅방 입장 성공:', result);
            } else {
                console.log('채팅방 입장 성공: 204 No Content');
            }

            console.log('Redirecting to room.html with roomId:', roomId);
            localStorage.setItem('currentRoomId', roomId);
            window.location.href = `/room.html`; // room.html로 리디렉션
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
            console.log('관리자와 1:1 채팅방 생성 성공:', result);
            alert('관리자와의 1:1 채팅방이 생성되었습니다.');
            // TODO: 생성된 채팅방으로 이동 또는 웹소켓 연결 로직 추가
        } catch (error) {
            console.error('관리자와 1:1 채팅방 생성 중 오류 발생:', error);
            alert('관리자와의 1:1 채팅방 생성에 실패했습니다.');
        }
    }

    

    // 페이지 로드 시 채팅방 목록 로드
    loadChatRooms();
});
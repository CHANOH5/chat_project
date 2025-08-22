document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    const senderId = localStorage.getItem('userId');
    const chatRoomId = localStorage.getItem('currentRoomId');

    if (!token || !senderId || !chatRoomId) {
        alert('인증 정보나 채팅방 정보가 없습니다. 로그인 페이지로 이동합니다.');
        window.location.href = '/index.html';
        return;
    }

    const messagesDisplay = document.getElementById('messagesDisplay');
    const messageInput = document.getElementById('messageInput');
    const sendMessageButton = document.getElementById('sendMessageButton');
    const participantList = document.getElementById('participantList');

    // 웹소켓 연결
    const wsUrl = `ws://localhost:8080/api/v1/ws/chat?token=${token}`;
    const ws = new WebSocket(wsUrl);

    function leaveChatRoom() {
        if (ws.readyState === WebSocket.OPEN) {
            const leaveMessage = {
                chatRoomId: chatRoomId,
                senderId: senderId,
                receiverId: null,
                content: `${senderId}님이 퇴장하셨습니다.`,
                messageType: 'TEXT',
                actionType: 'LEAVE'
            };
            ws.send(JSON.stringify(leaveMessage));
            ws.close();
        }
    }

    async function fetchParticipants() {
        try {
            const response = await fetch(`http://localhost:8080/api/v1/chat_room/${chatRoomId}/participants`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const participants = await response.json();
            participantList.innerHTML = '';
            participants.forEach(participant => {
                const li = document.createElement('li');
                li.textContent = participant;
                participantList.appendChild(li);
            });
        } catch (error) {
            console.error('참가자 목록 로드 중 오류 발생:', error);
        }
    }

    function appendMessage(text, type) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', type);
        messageElement.textContent = text;
        messagesDisplay.appendChild(messageElement);
        messagesDisplay.scrollTop = messagesDisplay.scrollHeight;
    }

    ws.onopen = () => {
        console.log('WebSocket connection established.');
        fetchParticipants();
        const enterMessage = {
            chatRoomId: chatRoomId,
            senderId: senderId,
            receiverId: null,
            content: `${senderId}님이 입장하셨습니다.`,
            messageType: 'TEXT',
            actionType: 'ENTER'
        };
        ws.send(JSON.stringify(enterMessage));
    };

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        let displayMessage;
        let messageTypeClass = 'received';

        switch (msg.actionType) {
            case 'ENTER':
                displayMessage = `${msg.senderId}님이 입장했습니다.`;
                messageTypeClass = 'system';
                fetchParticipants();
                break;
            case 'LEAVE':
                displayMessage = `${msg.senderId}님이 퇴장했습니다.`;
                messageTypeClass = 'system';
                fetchParticipants();
                break;
            case 'TALK':
                if (msg.senderId === senderId) {
                    messageTypeClass = 'sent';
                    displayMessage = msg.content;
                } else {
                    displayMessage = `${msg.senderId}: ${msg.content}`;
                }
                break;
            default:
                return;
        }
        appendMessage(displayMessage, messageTypeClass);
    };

    ws.onerror = (error) => {
        console.error('WebSocket Error:', error);
        alert('웹소켓 연결에 오류가 발생했습니다.');
    };

    ws.onclose = (event) => {
        if (!event.wasClean) {
            alert('웹소켓 연결이 비정상적으로 종료되었습니다.');
        }
    };

    function sendMessage() {
        const content = messageInput.value.trim();
        if (content && ws.readyState === WebSocket.OPEN) {
            const talkMessage = {
                chatRoomId: chatRoomId,
                senderId: senderId,
                receiverId: null,
                content: content,
                messageType: 'TEXT',
                actionType: 'TALK'
            };
            ws.send(JSON.stringify(talkMessage));
            appendMessage(content, 'sent');
            messageInput.value = '';
        }
    }

    sendMessageButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    window.addEventListener('beforeunload', leaveChatRoom);

    function setupHeaderButtons() {
        const backButton = document.getElementById('backButton');
        const logoutButton = document.getElementById('logoutButton');
        const roomNameHeader = document.getElementById('header-title');

        if(roomNameHeader) {
            roomNameHeader.textContent = `채팅방: ${chatRoomId}`;
        }

        if (backButton) {
            backButton.classList.remove('hidden');
            backButton.addEventListener('click', () => {
                leaveChatRoom();
                window.location.href = '/chat.html';
            });
        }

        if (logoutButton) {
            const originalLogout = logoutButton.onclick;
            logoutButton.onclick = null; // 기존 이벤트 제거
            logoutButton.addEventListener('click', () => {
                leaveChatRoom();
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
});

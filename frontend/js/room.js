document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('token');
    // userId는 로그인 시 저장되었다고 가정합니다. 실제 key 이름에 맞게 수정이 필요할 수 있습니다.
    const senderId = localStorage.getItem('userId');
    const chatRoomId = localStorage.getItem('currentRoomId');

    if (!token || !senderId || !chatRoomId) {
        alert('인증 정보나 채팅방 정보가 없습니다. 로그인 페이지로 이동합니다.');
        window.location.href = '/index.html';
        return;
    }

    const roomNameHeader = document.getElementById('roomNameHeader');
    const backButton = document.getElementById('backButton');
    const logoutButton = document.getElementById('logoutButton');
    const messagesDisplay = document.getElementById('messagesDisplay');
    const messageInput = document.getElementById('messageInput');
    const sendMessageButton = document.getElementById('sendMessageButton');

    roomNameHeader.textContent = `채팅방: ${chatRoomId}`; // 실제 방 이름으로 교체 필요

    // 웹소켓 연결
    const wsUrl = `ws://localhost:8080/api/v1/ws/chat?token=${token}`;
    const ws = new WebSocket(wsUrl);

    ws.onopen = () => {
        console.log('WebSocket connection established.');
        // 입장 메시지 전송
        const enterMessage = {
            chatRoomId: chatRoomId,
            senderId: senderId,
            receiverId: null, // 특정 수신자가 아닌, 방 전체에 대한 메시지
            content: `${senderId}님이 입장하셨습니다.`,
            messageType: 'TEXT',
            actionType: 'ENTER'
        };
        ws.send(JSON.stringify(enterMessage));
    };

    ws.onmessage = (event) => {
        const msg = JSON.parse(event.data);
        console.log('Message received:', msg);

        let displayMessage;
        let messageTypeClass = 'received'; // 기본은 받은 메시지

        switch (msg.actionType) {
            case 'ENTER':
                displayMessage = `${msg.senderId}님이 입장했습니다.`;
                messageTypeClass = 'system';
                break;
            case 'LEAVE':
                displayMessage = `${msg.senderId}님이 퇴장했습니다.`;
                messageTypeClass = 'system';
                break;
            case 'TALK':
                // 내가 보낸 메시지인지 확인
                if (msg.senderId === senderId) {
                    messageTypeClass = 'sent';
                    displayMessage = `${msg.content}`; // 보낸 메시지는 내용만 표시
                } else {
                    messageTypeClass = 'received';
                    displayMessage = `${msg.senderId}: ${msg.content}`;
                }
                break;
            default:
                console.warn('Unknown message action type:', msg.actionType);
                return; // 알 수 없는 타입은 표시하지 않음
        }

        appendMessage(displayMessage, messageTypeClass);
    };

    ws.onerror = (error) => {
        console.error('WebSocket Error:', error);
        alert('웹소켓 연결에 오류가 발생했습니다.');
    };

    ws.onclose = (event) => {
        console.log('WebSocket connection closed:', event);
        // 비정상적 종료 시 알림
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
                receiverId: null, // 특정 상대에게 보내는 것이 아니므로 null
                content: content,
                messageType: 'TEXT',
                actionType: 'TALK'
            };
            ws.send(JSON.stringify(talkMessage));
            // 내가 보낸 메시지를 화면에 바로 표시
            appendMessage(content, 'sent'); // 서버에서 다시 받을 것이므로 중복 표시 안함
            messageInput.value = '';
        }
    }

    sendMessageButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
        }
    });

    function appendMessage(text, type) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', type);
        messageElement.textContent = text;
        messagesDisplay.appendChild(messageElement);
        messagesDisplay.scrollTop = messagesDisplay.scrollHeight;
    }

    // 뒤로가기, 로그아웃, 페이지 이탈 시 퇴장 메시지 전송
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

    backButton.addEventListener('click', () => {
        leaveChatRoom();
        window.location.href = '/chat.html';
    });

    logoutButton.addEventListener('click', () => {
        leaveChatRoom();
        localStorage.clear(); // 로그아웃 시 모든 정보 삭제
        window.location.href = '/index.html';
    });

    // 사용자가 페이지를 떠날 때 (새로고침, 탭 닫기 등)
    window.addEventListener('beforeunload', leaveChatRoom);
});
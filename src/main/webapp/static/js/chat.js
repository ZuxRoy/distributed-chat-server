let stompClient = null;
let currentUser = null;
let currentChat = null;
let currentChatType = null; // 'direct' or 'group'

// Show login modal on page load
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('loginModal').style.display = 'block';
});

function handleLoginKeyPress(event) {
    if (event.key === 'Enter') {
        login();
    }
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

async function login() {
    const username = document.getElementById('usernameInput').value.trim();
    if (!username) {
        alert('Please enter a username');
        return;
    }

    try {
        // Register user
        const response = await fetch('/api/users/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username: username })
        });

        if (response.ok || response.status === 400) {
            // User registered or already exists
            currentUser = username;
            document.getElementById('currentUser').innerHTML = 
                `<span class="online-indicator"></span>${username}`;
            document.getElementById('loginModal').style.display = 'none';
            connect();
        } else {
            alert('Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Registration failed: ' + error.message);
    }
}

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        updateConnectionStatus(true);
        
        // Subscribe to personal messages
        stompClient.subscribe('/user/queue/messages', function (message) {
            const messageData = JSON.parse(message.body);
            displayMessage(messageData);
        });
        
        // Notify server about user connection
        stompClient.send("/app/chat.addUser", {}, JSON.stringify({
            senderUsername: currentUser,
            messageType: 'JOIN'
        }));
        
    }, function (error) {
        console.log('Connection error: ' + error);
        updateConnectionStatus(false);
        setTimeout(connect, 5000); // Reconnect after 5 seconds
    });
}

function updateConnectionStatus(connected) {
    const statusElement = document.getElementById('connectionStatus');
    if (connected) {
        statusElement.textContent = 'Connected';
        statusElement.className = 'connection-status';
    } else {
        statusElement.textContent = 'Disconnected';
        statusElement.className = 'connection-status disconnected';
    }
}

function startDirectMessage() {
    const targetUser = document.getElementById('targetUser').value.trim();
    if (!targetUser) {
        alert('Please enter a username');
        return;
    }
    
    if (targetUser === currentUser) {
        alert('Cannot start a conversation with yourself');
        return;
    }

    // Check if chat already exists
    const existingChat = document.querySelector(`[data-chat-id="${targetUser}"]`);
    if (existingChat) {
        selectChat(targetUser, 'direct');
        return;
    }

    // Add to chat list
    addChatToList(targetUser, 'direct', targetUser);
    selectChat(targetUser, 'direct');
    document.getElementById('targetUser').value = '';
}

async function createGroup() {
    const groupName = document.getElementById('groupName').value.trim();
    if (!groupName) {
        alert('Please enter a group name');
        return;
    }

    try {
        const response = await fetch('/api/groups', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                name: groupName,
                createdBy: currentUser
            })
        });

        if (response.ok) {
            const group = await response.json();
            
            // Add to chat list
            addChatToList(group.id, 'group', groupName);
            selectChat(group.id, 'group');
            
            // Show group ID modal
            document.getElementById('createdGroupId').textContent = group.id;
            document.getElementById('groupModal').style.display = 'block';
            
            document.getElementById('groupName').value = '';
        } else {
            const error = await response.text();
            alert('Failed to create group: ' + error);
        }
    } catch (error) {
        console.error('Group creation error:', error);
        alert('Failed to create group: ' + error.message);
    }
}

async function joinGroup() {
    const groupId = document.getElementById('joinGroupId').value.trim();
    if (!groupId) {
        alert('Please enter a group ID');
        return;
    }

    try {
        const response = await fetch(`/api/groups/${groupId}/members`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                username: currentUser
            })
        });

        if (response.ok) {
            // Add to chat list
            addChatToList(groupId, 'group', `Group #${groupId}`);
            selectChat(groupId, 'group');
            document.getElementById('joinGroupId').value = '';
        } else {
            const error = await response.text();
            alert('Failed to join group: ' + error);
        }
    } catch (error) {
        console.error('Join group error:', error);
        alert('Failed to join group: ' + error.message);
    }
}

function closeGroupModal() {
    document.getElementById('groupModal').style.display = 'none';
}

function addChatToList(chatId, type, displayName) {
    const chatList = document.getElementById('chatList');
    
    const chatItem = document.createElement('div');
    chatItem.className = 'chat-item';
    chatItem.setAttribute('data-chat-id', chatId);
    chatItem.setAttribute('data-chat-type', type);
    chatItem.onclick = () => selectChat(chatId, type);
    
    const indicator = type === 'direct' ? 
        '<span class="online-indicator"></span>' : 
        '<span style="margin-right: 5px;">👥</span>';
    
    chatItem.innerHTML = `${indicator}${displayName}`;
    chatList.appendChild(chatItem);
}

function selectChat(chatId, type) {
    // Update UI
    document.querySelectorAll('.chat-item').forEach(item => {
        item.classList.remove('active');
    });
    
    const selectedItem = document.querySelector(`[data-chat-id="${chatId}"]`);
    if (selectedItem) {
        selectedItem.classList.add('active');
    }

    currentChat = chatId;
    currentChatType = type;

    // Update chat header
    const chatTitle = document.getElementById('chatTitle');
    const chatSubtitle = document.getElementById('chatSubtitle');
    
    if (type === 'direct') {
        chatTitle.textContent = chatId;
        chatSubtitle.textContent = 'Direct Message';
    } else {
        chatTitle.textContent = `Group #${chatId}`;
        chatSubtitle.textContent = 'Group Chat';
        
        // Subscribe to group messages
        if (stompClient && stompClient.connected) {
            stompClient.subscribe(`/topic/group/${chatId}`, function (message) {
                const messageData = JSON.parse(message.body);
                if (currentChat == chatId && currentChatType === 'group') {
                    displayMessage(messageData);
                }
            });
        }
    }

    // Clear messages
    clearMessages();
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const content = messageInput.value.trim();
    
    if (!content || !currentChat || !stompClient) {
        return;
    }

    const message = {
        senderUsername: currentUser,
        content: content,
        messageType: currentChatType === 'direct' ? 'DIRECT' : 'GROUP'
    };

    if (currentChatType === 'direct') {
        message.receiverUsername = currentChat;
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(message));
    } else {
        message.groupId = parseInt(currentChat);
        stompClient.send("/app/chat.sendGroupMessage", {}, JSON.stringify(message));
    }

    // Display message immediately for sender
    displayMessage(message);
    messageInput.value = '';
}

function displayMessage(messageData) {
    const messagesContainer = document.getElementById('messagesContainer');
    
    // Clear welcome message if it exists
    if (messagesContainer.children.length === 1 && 
        messagesContainer.children[0].style.textAlign === 'center') {
        messagesContainer.innerHTML = '';
    }

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    
    const isOwnMessage = messageData.senderUsername === currentUser;
    if (isOwnMessage) {
        messageDiv.classList.add('own');
    }

    const messageContent = document.createElement('div');
    messageContent.className = 'message-content';
    
    if (!isOwnMessage) {
        const senderDiv = document.createElement('div');
        senderDiv.className = 'message-sender';
        senderDiv.textContent = messageData.senderUsername;
        messageContent.appendChild(senderDiv);
    }
    
    const contentDiv = document.createElement('div');
    contentDiv.textContent = messageData.content;
    messageContent.appendChild(contentDiv);
    
    const timeDiv = document.createElement('div');
    timeDiv.className = 'message-time';
    timeDiv.textContent = new Date().toLocaleTimeString();
    messageContent.appendChild(timeDiv);
    
    messageDiv.appendChild(messageContent);
    messagesContainer.appendChild(messageDiv);
    
    // Scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function clearMessages() {
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '';
}

// Handle page unload
window.addEventListener('beforeunload', function() {
    if (stompClient && stompClient.connected) {
        stompClient.disconnect();
    }
});

// Handle connection errors
window.addEventListener('online', function() {
    if (!stompClient || !stompClient.connected) {
        connect();
    }
});

window.addEventListener('offline', function() {
    updateConnectionStatus(false);
});

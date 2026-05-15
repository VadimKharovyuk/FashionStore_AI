// // sessionId передається з Thymeleaf через data-атрибут на body
// const sessionId = document.body.dataset.sessionId;
// const messages  = document.getElementById('messages');
// const input     = document.getElementById('user-input');
// const sendBtn   = document.getElementById('send-btn');
//
// // ── Auto-resize textarea ──────────────────────────────────────
// input.addEventListener('input', () => {
//     input.style.height = 'auto';
//     input.style.height = Math.min(input.scrollHeight, 160) + 'px';
// });
//
// // ── Enter to send ─────────────────────────────────────────────
// input.addEventListener('keydown', e => {
//     if (e.key === 'Enter' && !e.shiftKey) {
//         e.preventDefault();
//         send();
//     }
// });
//
//
// sendBtn.addEventListener('click', send);
//
// // ── Send ──────────────────────────────────────────────────────
// function send() {
//     const text = input.value.trim();
//     if (!text) return;
//
//     appendMessage('user', text);
//     input.value = '';
//     input.style.height = 'auto';
//     setLoading(true);
//
//     const typingEl = appendTyping();
//     streamResponse(text, typingEl);
// }
//
// // ── Fetch streaming (POST + ReadableStream) ───────────────────
// async function streamResponse(text, typingEl) {
//     let bubbleEl = null;
//     let rawText  = '';
//
//     try {
//         const res = await fetch('/api/chat/stream', {
//             method: 'POST',
//             headers: { 'Content-Type': 'application/json' },
//             body: JSON.stringify({ message: text })
//         });
//
//         const reader  = res.body.getReader();
//         const decoder = new TextDecoder();
//         let buf = '';
//
//         while (true) {
//             const { done, value } = await reader.read();
//             if (done) break;
//
//             buf += decoder.decode(value, { stream: true });
//             const lines = buf.split('\n');
//             buf = lines.pop();
//
//             for (const line of lines) {
//                 if (!line.startsWith('data:')) continue;
//                 let chunk;
//                 try { chunk = JSON.parse(line.substring(5)); } catch { continue; }
//
//                 if (chunk.type === 'token') {
//                     if (!bubbleEl) {
//                         typingEl.remove();
//                         bubbleEl = appendMessage('assistant', '');
//                         setLoading(false);
//                     }
//                     rawText += chunk.content;
//                     bubbleEl.querySelector('.bubble').innerHTML = renderMd(rawText);
//                     scrollBottom();
//                 } else if (chunk.type === 'error') {
//                     typingEl.remove();
//                     appendMessage('assistant', '⚠️ ' + chunk.content);
//                     setLoading(false);
//                 } else if (chunk.type === 'done') {
//                     if (!bubbleEl) { typingEl.remove(); setLoading(false); }
//                 }
//             }
//         }
//     } catch (e) {
//         typingEl.remove();
//         appendMessage('assistant', "⚠️ Помилка з'єднання. Спробуй ще раз.");
//         setLoading(false);
//     }
// }
//
// // ── Markdown рендер ───────────────────────────────────────────
// const MD_LINK = new RegExp('\\[([^\\]]+)\\]\\((/[^)]+)\\)', 'g');
//
// function renderMd(text) {
//     // спочатку замінюємо ID посилання ДО escaping
//     let result = text
//         // паттерн "Назва (Бренд, ID:15)" → клікабельна назва
//         .replace(/([^(\n]+)\([^,)]+,\s*ID:(\d+)\)/g,
//             (_, name, id) => '[' + name.trim() + '](/products/' + id + ')')
//         // просто "ID:15" → посилання
//         .replace(/\bID:(\d+)\b/g,
//             '[переглянути #$1](/products/$1)');
//
//     // тепер безпечний escape і решта markdown
//     return result
//         .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
//         .replace(MD_LINK, '<a href="$2" class="text-dark fw-semibold" target="_blank">$1</a>')
//         .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
//         .replace(/\*(.+?)\*/g, '<em>$1</em>')
//         .replace(/\n/g, '<br>');
// }
//
// // ── DOM helpers ───────────────────────────────────────────────
// function appendMessage(role, text) {
//     const isUser = role === 'user';
//     const wrap   = document.createElement('div');
//     wrap.className = `d-flex mb-3 px-2 ${isUser
//         ? 'msg-user justify-content-end'
//         : 'msg-assistant justify-content-start'}`;
//
//     const now = new Date().toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' });
//
//     wrap.innerHTML = `
//         <div>
//             ${!isUser ? `<div class="text-muted agent-badge mb-1 ms-1">
//                 <i class="bi bi-cpu me-1"></i>Shopping Assistant</div>` : ''}
//             <div class="bubble">${escHtml(text)}</div>
//             <div class="text-muted mt-1 px-1 ${isUser ? 'text-end' : ''}"
//                  style="font-size:.7rem;">${now}</div>
//         </div>`;
//
//     messages.appendChild(wrap);
//     scrollBottom();
//     return wrap;
// }
//
// function appendTyping() {
//     const wrap = document.createElement('div');
//     wrap.className = 'd-flex mb-3 px-2 msg-assistant justify-content-start';
//     wrap.innerHTML = `
//         <div>
//             <div class="bubble d-flex align-items-center gap-1" style="min-width:56px;">
//                 <div class="typing-dot"></div>
//                 <div class="typing-dot"></div>
//                 <div class="typing-dot"></div>
//             </div>
//         </div>`;
//     messages.appendChild(wrap);
//     scrollBottom();
//     return wrap;
// }
//
// function setLoading(on) {
//     sendBtn.disabled = on;
//     input.disabled   = on;
// }
//
// function scrollBottom() {
//     messages.scrollTop = messages.scrollHeight;
// }
//
// function escHtml(str) {
//     return str.replace(/&/g, '&amp;').replace(/</g, '&lt;')
//         .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
// }
//
// // ── Scroll вниз при завантаженні ──────────────────────────────
// scrollBottom();


// sessionId передається з Thymeleaf через data-атрибут на body
const sessionId = document.body.dataset.sessionId;
const messages  = document.getElementById('messages');
const input     = document.getElementById('user-input');
const sendBtn   = document.getElementById('send-btn');

// ── Auto-resize textarea ──────────────────────────────────────
input.addEventListener('input', () => {
    input.style.height = 'auto';
    input.style.height = Math.min(input.scrollHeight, 160) + 'px';
});

// ── Enter to send ─────────────────────────────────────────────
input.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        send();
    }
});

sendBtn.addEventListener('click', send);

// ── Send ──────────────────────────────────────────────────────
function send() {
    const text = input.value.trim();
    if (!text) return;

    appendMessage('user', text);
    input.value = '';
    input.style.height = 'auto';
    setLoading(true);

    const typingEl = appendTyping();
    streamResponse(text, typingEl);
}

// ── Fetch streaming (POST + ReadableStream) ───────────────────
async function streamResponse(text, typingEl) {
    let bubbleEl = null;
    let rawText  = '';

    try {
        const res = await fetch('/api/chat/stream', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: text })
        });

        const reader  = res.body.getReader();
        const decoder = new TextDecoder();
        let buf = '';

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            buf += decoder.decode(value, { stream: true });
            const lines = buf.split('\n');
            buf = lines.pop();

            for (const line of lines) {
                if (!line.startsWith('data:')) continue;
                let chunk;
                try { chunk = JSON.parse(line.substring(5)); } catch { continue; }

                if (chunk.type === 'token') {
                    if (!bubbleEl) {
                        typingEl.remove();
                        bubbleEl = appendMessage('assistant', '');
                        setLoading(false);
                    }
                    rawText += chunk.content;
                    bubbleEl.querySelector('.bubble').innerHTML = renderMd(rawText);
                    scrollBottom();
                } else if (chunk.type === 'error') {
                    typingEl.remove();
                    appendMessage('assistant', '⚠️ ' + chunk.content);
                    setLoading(false);
                } else if (chunk.type === 'done') {
                    if (!bubbleEl) { typingEl.remove(); setLoading(false); }
                }
            }
        }
    } catch (e) {
        typingEl.remove();
        appendMessage('assistant', "⚠️ Помилка з'єднання. Спробуй ще раз.");
        setLoading(false);
    }
}

// ── Markdown рендер ───────────────────────────────────────────
const MD_LINK = new RegExp('\\[([^\\]]+)\\]\\((/[^)]+)\\)', 'g');

function renderMd(text) {
    // спочатку замінюємо ID посилання ДО escaping
    let result = text
        // паттерн "Назва (Бренд, ID:15)" → клікабельна назва
        .replace(/([^(\n]+)\([^,)]+,\s*ID:(\d+)\)/g,
            (_, name, id) => '[' + name.trim() + '](/products/' + id + ')')
        // просто "ID:15" → посилання
        .replace(/\bID:(\d+)\b/g,
            '[переглянути #$1](/products/$1)');

    // тепер безпечний escape і решта markdown
    return result
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(MD_LINK, '<a href="$2" class="text-dark fw-semibold" target="_blank">$1</a>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/\n/g, '<br>');
}

// ── DOM helpers ───────────────────────────────────────────────
function appendMessage(role, text) {
    const isUser = role === 'user';
    const wrap   = document.createElement('div');
    wrap.className = `d-flex mb-3 px-2 ${isUser
        ? 'msg-user justify-content-end'
        : 'msg-assistant justify-content-start'}`;

    const now = new Date().toLocaleTimeString('uk-UA', { hour: '2-digit', minute: '2-digit' });

    wrap.innerHTML = `
        <div>
            ${!isUser ? `<div class="text-muted agent-badge mb-1 ms-1">
                <i class="bi bi-cpu me-1"></i>Shopping Assistant</div>` : ''}
            <div class="bubble">${escHtml(text)}</div>
            <div class="text-muted mt-1 px-1 ${isUser ? 'text-end' : ''}"
                 style="font-size:.7rem;">${now}</div>
        </div>`;

    messages.appendChild(wrap);
    scrollBottom();
    return wrap;
}

function appendTyping() {
    const wrap = document.createElement('div');
    wrap.className = 'd-flex mb-3 px-2 msg-assistant justify-content-start';
    wrap.innerHTML = `
        <div>
            <div class="bubble d-flex align-items-center gap-1" style="min-width:56px;">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        </div>`;
    messages.appendChild(wrap);
    scrollBottom();
    return wrap;
}

function setLoading(on) {
    sendBtn.disabled = on;
    input.disabled   = on;
}

function scrollBottom() {
    messages.scrollTop = messages.scrollHeight;
}

function escHtml(str) {
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// ── Очистити історію ──────────────────────────────────────────
async function clearHistory() {
    if (!confirm('Очистити історію чату? Summary буде збережено.')) return;
    try {
        await fetch('/api/chat/history', { method: 'DELETE' });
        // очищаємо UI
        const messagesEl = document.getElementById('messages');
        messagesEl.innerHTML = `
            <div class="text-center text-muted py-5">
                <i class="bi bi-bag-heart" style="font-size:2rem;"></i>
                <p class="mt-3 mb-1 fw-semibold">Привіт! Я допоможу знайти одяг</p>
                <p style="font-size:.85rem;">Скажи що шукаєш — категорію, колір, бренд або бюджет</p>
            </div>`;
    } catch (e) {
        console.error('clearHistory error:', e);
    }
}

// ── Нова сесія ────────────────────────────────────────────────
async function resetSession() {
    if (!confirm('Почати нову сесію? Вся історія буде закрита.')) return;
    try {
        await fetch('/api/chat/reset', { method: 'POST' });
        window.location.reload(); // перезавантажуємо — нова сесія створюється автоматично
    } catch (e) {
        console.error('resetSession error:', e);
    }
}

// ── Scroll вниз при завантаженні ──────────────────────────────
scrollBottom();
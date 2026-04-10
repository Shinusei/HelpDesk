const BASE_URL = '/api';

async function request(path, options = {}) {
    const response = await fetch(`${BASE_URL}${path}`, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });

    if (response.status === 401) {
        window.location.href = '/login';
        return;
    }

    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: 'An error occurred' }));
        throw new Error(error.message || 'Request failed');
    }

    if (response.status === 204) return null;
    return response.json();
}

export const api = {
    user: {
        me: () => request('/user/me'),
    },
    tickets: {
        list: (sort = 'priorityScore', dir = 'desc') => request(`/tickets?sort=${sort}&dir=${dir}`),
        get: (id) => request(`/tickets/${id}`),
        create: (data) => request('/tickets', { method: 'POST', body: JSON.stringify(data) }),
        updateStatus: (id, status, resolution) => request(`/tickets/${id}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status, resolution })
        }),
        getComments: (id) => request(`/tickets/${id}/comments`),
        addComment: (id, text) => request(`/tickets/${id}/comments`, {
            method: 'POST',
            body: JSON.stringify({ text })
        }),
        assignToMe: (id) => request(`/tickets/${id}/assign-to-me`, { method: 'PATCH' }),

        uploadAttachment: async (ticketId, file) => {
            const formData = new FormData();
            formData.append('file', file);
            const response = await fetch(`${BASE_URL}/tickets/${ticketId}/attachments`, {
                method: 'POST',
                body: formData
                // Do NOT set Content-Type; browser sets it with boundary for multipart
            });
            if (!response.ok) throw new Error('Upload failed');
            return response.json();
        },

        downloadAttachmentUrl: (attachmentId) => `${BASE_URL}/tickets/attachments/${attachmentId}`,

        deleteAttachment: (attachmentId) => request(`/tickets/attachments/${attachmentId}`, { method: 'DELETE' }),
    },
    admin: {
        dashboard: {
            stats: () => request('/admin/dashboard/stats'),
        },
        users: {
            list: () => request('/admin/users'),
            get: (id) => request(`/admin/users/${id}`),
            create: (data) => request('/admin/users', { method: 'POST', body: JSON.stringify(data) }),
            update: (id, data) => request(`/admin/users/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
            delete: (id) => request(`/admin/users/${id}`, { method: 'DELETE' }),
            roles: () => request('/admin/users/roles'),
        },
        priority: {
            list: () => request('/admin/priority-weights'),
            update: (id, data) => request(`/admin/priority-weights/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
        }
    }
};

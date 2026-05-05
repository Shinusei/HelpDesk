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
        changePassword: (currentPassword, newPassword) => request('/user/me/password', {
            method: 'PATCH',
            body: JSON.stringify({ currentPassword, newPassword })
        }),
    },
    tickets: {
        list: (includeClosed = true, sort = 'priorityScore', dir = 'desc') =>
            request(`/tickets?includeClosed=${includeClosed}&sort=${encodeURIComponent(sort)}&dir=${encodeURIComponent(dir)}`),
        assignedToMe: (includeClosed = false, sort = 'priorityScore', dir = 'desc') =>
            request(`/tickets/assigned-to-me?includeClosed=${includeClosed}&sort=${encodeURIComponent(sort)}&dir=${encodeURIComponent(dir)}`),
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
        unassignMe: (id) => request(`/tickets/${id}/unassign-me`, { method: 'PATCH' }),
        autoAssign: () => request('/tickets/auto-assign', { method: 'POST' }),

        uploadAttachment: async (ticketId, file) => {
            const formData = new FormData();
            formData.append('file', file);
            const response = await fetch(`${BASE_URL}/tickets/${ticketId}/attachments`, {
                method: 'POST',
                body: formData
            });
            if (!response.ok) throw new Error('Upload failed');
            return response.json();
        },

        uploadCommentAttachment: async (ticketId, commentId, file) => {
            const formData = new FormData();
            formData.append('file', file);
            const response = await fetch(`${BASE_URL}/tickets/${ticketId}/comments/${commentId}/attachments`, {
                method: 'POST',
                body: formData
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
        tickets: {
            assignSupport: (ticketId, executorUsername) => request(`/admin/tickets/${ticketId}/assign`, {
                method: 'PATCH',
                body: JSON.stringify({ executorUsername })
            }),
            byExecutor: (username, includeClosed = false, sort = 'priorityScore', dir = 'desc') =>
                request(`/admin/tickets/by-executor?username=${encodeURIComponent(username)}&includeClosed=${includeClosed}&sort=${encodeURIComponent(sort)}&dir=${encodeURIComponent(dir)}`),
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
            reset: () => request('/admin/priority-weights/reset', { method: 'POST' }),
        },
        parameterValues: {
            listAll: () => request('/admin/parameter-values'),
            listByParam: (paramName) => request(`/admin/parameter-values/by-param?paramName=${paramName}`),
            update: (id, data) => request(`/admin/parameter-values/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
            init: () => request('/admin/parameter-values/init', { method: 'POST' }),
        },
        dynamicFilters: {
            list: () => request('/admin/dynamic-filters'),
            create: (data) => request('/admin/dynamic-filters', { method: 'POST', body: JSON.stringify(data) }),
            update: (id, data) => request(`/admin/dynamic-filters/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
            delete: (id) => request(`/admin/dynamic-filters/${id}`, { method: 'DELETE' }),
            addValue: (filterId, data) => request(`/admin/dynamic-filters/${filterId}/values`, { method: 'POST', body: JSON.stringify(data) }),
            updateValue: (valueId, data) => request(`/admin/dynamic-filters/values/${valueId}`, { method: 'PATCH', body: JSON.stringify(data) }),
            deleteValue: (valueId) => request(`/admin/dynamic-filters/values/${valueId}`, { method: 'DELETE' }),
        }
    },
    priority: {
        list: () => request('/priority-weights'),
    },
    dynamicFilters: {
        list: () => request('/dynamic-filters'),
    }
};

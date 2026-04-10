export function formatDate(dateString) {
    if (!dateString) return '—';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

export function getStatusColor(status) {
    switch (status) {
        case 'NEW': return 'bg-blue-100 text-blue-800 border-blue-200';
        case 'IN_PROGRESS': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
        case 'CLOSED': return 'bg-green-100 text-green-800 border-green-200';
        default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
}

export function getPriorityColor(score) {
    if (score >= 100) return 'text-red-600 font-bold';
    if (score >= 50) return 'text-orange-600 font-semibold';
    return 'text-green-600';
}

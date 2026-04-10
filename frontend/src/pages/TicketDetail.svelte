<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { api } from '../lib/api';
    import { formatDate, getStatusColor, getPriorityColor } from '../lib/utils';

    export let params;
    export let user = null;
    const ticketId = params.id;
    const queryClient = useQueryClient();

    const ticketQuery = createQuery({
        queryKey: ['ticket', ticketId],
        queryFn: () => api.tickets.get(ticketId),
    });

    const commentsQuery = createQuery({
        queryKey: ['comments', ticketId],
        queryFn: () => api.tickets.getComments(ticketId),
    });

    const statusMutation = createMutation({
        mutationFn: ({ status, resolution }) => api.tickets.updateStatus(ticketId, status, resolution),
        onSuccess: () => {
            queryClient.invalidateQueries(['ticket', ticketId]);
            showResolutionModal = false;
        }
    });

    const commentMutation = createMutation({
        mutationFn: (text) => api.tickets.addComment(ticketId, text),
        onSuccess: () => {
            queryClient.invalidateQueries(['comments', ticketId]);
            newComment = '';
        }
    });

    const assignMutation = createMutation({
        mutationFn: () => api.tickets.assignToMe(ticketId),
        onSuccess: () => queryClient.invalidateQueries(['ticket', ticketId])
    });

    const uploadMutation = createMutation({
        mutationFn: (file) => api.tickets.uploadAttachment(ticketId, file),
        onSuccess: () => {
            queryClient.invalidateQueries(['ticket', ticketId]);
            uploadFile = null;
        }
    });

    const deleteAttachmentMutation = createMutation({
        mutationFn: (attachmentId) => api.tickets.deleteAttachment(attachmentId),
        onSuccess: () => queryClient.invalidateQueries(['ticket', ticketId])
    });

    let newComment = '';
    let showResolutionModal = false;
    let resolutionText = '';
    let uploadFile = null;

    const statusMap = {
        'NEW': 'Новый',
        'IN_PROGRESS': 'В работе',
        'CLOSED': 'Закрыт'
    };

    const labelMap = {
        urgency: { LOW: 'Низкая', MEDIUM: 'Средняя', HIGH: 'Высокая', CRITICAL: 'Критическая' },
        importance: { LOW: 'Низкая', MEDIUM: 'Средняя', HIGH: 'Высокая', CRITICAL: 'Критическая' },
        impact: { USER: 'Пользователь', DEPARTMENT: 'Отдел', ORGANIZATION: 'Организация' },
        category: { HARDWARE: 'Оборудование', SOFTWARE: 'ПО', NETWORK: 'Сеть', ACCESS: 'Доступ', OTHER: 'Прочее' },
    };

    function handleUpdateStatus(status) {
        if (status === 'CLOSED') {
            showResolutionModal = true;
        } else {
            $statusMutation.mutate({ status });
        }
    }

    function submitResolution() {
        if (resolutionText.trim()) {
            $statusMutation.mutate({ status: 'CLOSED', resolution: resolutionText });
        }
    }

    function handleFileSelect(event) {
        uploadFile = event.target.files[0] || null;
    }

    function handleUpload() {
        if (uploadFile) $uploadMutation.mutate(uploadFile);
    }

    function confirmDeleteAttachment(id, name) {
        if (confirm(`Удалить файл «${name}»?`)) {
            $deleteAttachmentMutation.mutate(id);
        }
    }

    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    // Role helpers — use the "short" form that comes from UserDto (ADMIN, IT_SUPPORT, EMPLOYEE…)
    $: isSupport = user?.role === 'ADMIN' || user?.role === 'IT_SUPPORT' || user?.role === 'ROLE_ADMIN' || user?.role === 'ROLE_IT_SUPPORT';
    $: isRegularUser = !isSupport;
</script>

<div class="space-y-8 pb-20">
    <!-- Ticket Card -->
    <div class="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
        {#if $ticketQuery.isLoading}
            <div class="flex items-center justify-center py-24">
                <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
            </div>
        {:else if $ticketQuery.data}
            <!-- Header -->
            <div class="p-8 border-b border-gray-100 bg-gradient-to-br from-indigo-50/50 to-white">
                <div class="flex flex-col md:flex-row justify-between gap-6 items-start">
                    <div class="space-y-3 max-w-2xl">
                        <div class="flex flex-wrap gap-2 items-center">
                            <span class="px-3 py-1 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-lg">#{$ticketQuery.data.id}</span>
                            <span class="px-3 py-1 rounded-lg text-xs font-bold uppercase border {getStatusColor($ticketQuery.data.status)}">
                                {statusMap[$ticketQuery.data.status] || $ticketQuery.data.status}
                            </span>
                        </div>
                        <h1 class="text-3xl font-extrabold text-gray-900 leading-tight">{$ticketQuery.data.title}</h1>
                        <p class="text-gray-500 text-sm">
                            Создано {formatDate($ticketQuery.data.createdAt)}
                            &nbsp;·&nbsp; <span class="font-medium text-gray-700">Автор: {$ticketQuery.data.creatorName}</span>
                        </p>
                    </div>

                    <!-- Action Buttons (support/admin only for assign) -->
                    <div class="flex flex-wrap gap-3 shrink-0">
                        {#if $ticketQuery.data.status !== 'CLOSED'}
                            <button on:click={() => handleUpdateStatus('CLOSED')}
                                    class="bg-red-500 hover:bg-red-600 text-white px-5 py-2.5 rounded-xl font-bold text-sm shadow-lg shadow-red-100 transition-all transform hover:-translate-y-0.5">
                                Закрыть
                            </button>
                        {/if}
                        {#if isSupport && $ticketQuery.data.status === 'NEW'}
                            <button on:click={() => $assignMutation.mutate()}
                                    disabled={$assignMutation.isPending}
                                    class="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl font-bold text-sm shadow-lg shadow-indigo-100 transition-all transform hover:-translate-y-0.5 disabled:opacity-50">
                                Взять в работу
                            </button>
                        {/if}
                    </div>
                </div>
            </div>

            <!-- Body -->
            <div class="p-8 grid grid-cols-1 lg:grid-cols-3 gap-10">
                <!-- Description & Attachments -->
                <div class="lg:col-span-2 space-y-8">
                    <section>
                        <h4 class="text-xs font-black text-gray-400 uppercase tracking-[0.2em] mb-4">Описание</h4>
                        <div class="text-gray-700 leading-relaxed bg-gray-50 p-6 rounded-2xl border border-gray-100 whitespace-pre-wrap">
                            {$ticketQuery.data.description || 'Нет описания'}
                        </div>
                    </section>

                    {#if $ticketQuery.data.resolution}
                        <section class="bg-green-50 p-6 rounded-2xl border border-green-100">
                            <h4 class="text-xs font-black text-green-600 uppercase tracking-[0.2em] mb-4">Решение</h4>
                            <p class="text-green-900 font-medium leading-relaxed">{$ticketQuery.data.resolution}</p>
                        </section>
                    {/if}

                    <!-- Attachments Section -->
                    <section>
                        <h4 class="text-xs font-black text-gray-400 uppercase tracking-[0.2em] mb-4">Вложения</h4>

                        {#if $ticketQuery.data.attachments?.length > 0}
                            <div class="space-y-2 mb-4">
                                {#each $ticketQuery.data.attachments as att}
                                    <div class="flex items-center gap-3 bg-gray-50 rounded-xl px-4 py-3 border border-gray-100 group">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-indigo-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                                        </svg>
                                        <div class="flex-grow min-w-0">
                                            <p class="text-sm font-bold text-gray-800 truncate">{att.fileName}</p>
                                            <p class="text-xs text-gray-400">{formatFileSize(att.fileSize)}</p>
                                        </div>
                                        <a href={api.tickets.downloadAttachmentUrl(att.id)}
                                           download={att.fileName}
                                           class="p-1.5 text-indigo-500 hover:bg-indigo-50 rounded-lg transition-colors shrink-0"
                                           title="Скачать">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                                            </svg>
                                        </a>
                                        <button on:click={() => confirmDeleteAttachment(att.id, att.fileName)}
                                                class="p-1.5 text-red-400 hover:bg-red-50 rounded-lg transition-colors opacity-0 group-hover:opacity-100 shrink-0"
                                                title="Удалить">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                            </svg>
                                        </button>
                                    </div>
                                {/each}
                            </div>
                        {:else}
                            <p class="text-sm text-gray-400 mb-4">Вложений нет</p>
                        {/if}

                        <!-- Upload -->
                        {#if $ticketQuery.data.status !== 'CLOSED'}
                            <div class="flex gap-3 items-center">
                                <label for="file-upload" class="cursor-pointer flex items-center gap-2 bg-gray-100 hover:bg-gray-200 text-gray-600 px-4 py-2.5 rounded-xl text-sm font-bold transition-colors">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
                                    </svg>
                                    Выбрать файл
                                </label>
                                <input id="file-upload" type="file" class="hidden" on:change={handleFileSelect} />
                                {#if uploadFile}
                                    <span class="text-sm text-gray-600 font-medium truncate max-w-xs">{uploadFile.name}</span>
                                    <button on:click={handleUpload} disabled={$uploadMutation.isPending}
                                            class="bg-indigo-600 hover:bg-indigo-700 text-white px-5 py-2.5 rounded-xl text-sm font-black disabled:opacity-50 transition-colors shadow-lg shadow-indigo-100">
                                        {$uploadMutation.isPending ? 'Загрузка...' : 'Прикрепить'}
                                    </button>
                                {/if}
                            </div>
                        {/if}
                    </section>
                </div>

                <!-- Parameters Sidebar -->
                <div class="bg-gray-50/50 p-6 rounded-2xl border border-gray-100 space-y-6 self-start">
                    <h4 class="text-xs font-black text-gray-400 uppercase tracking-[0.2em]">Параметры</h4>

                    <!-- Priority — hidden for regular users -->
                    {#if !isRegularUser}
                        <div>
                            <span class="block text-xs font-bold text-gray-500 uppercase mb-1">Приоритет</span>
                            <span class="text-2xl font-black {getPriorityColor($ticketQuery.data.priorityScore)}">{$ticketQuery.data.priorityScore?.toFixed(2)}</span>
                        </div>
                    {/if}

                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Срочность</span>
                            <span class="text-sm font-bold">{labelMap.urgency[$ticketQuery.data.urgency] || $ticketQuery.data.urgency}</span>
                        </div>
                        <div>
                            <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Важность</span>
                            <span class="text-sm font-bold">{labelMap.importance[$ticketQuery.data.importance] || $ticketQuery.data.importance}</span>
                        </div>
                        <div>
                            <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Влияние</span>
                            <span class="text-sm font-bold">{labelMap.impact[$ticketQuery.data.impact] || $ticketQuery.data.impact}</span>
                        </div>
                        <div>
                            <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Категория</span>
                            <span class="text-sm font-bold">{labelMap.category[$ticketQuery.data.category] || $ticketQuery.data.category}</span>
                        </div>
                    </div>

                    <div>
                        <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Исполнитель</span>
                        <span class="text-sm font-medium text-indigo-600">{$ticketQuery.data.executorName}</span>
                    </div>
                    <div>
                        <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Дедлайн (SLA)</span>
                        <span class="text-sm font-medium {new Date($ticketQuery.data.slaDeadline) < new Date() && $ticketQuery.data.status !== 'CLOSED' ? 'text-red-500 font-bold' : 'text-gray-700'}">
                            {formatDate($ticketQuery.data.slaDeadline)}
                        </span>
                    </div>
                </div>
            </div>
        {/if}
    </div>

    <!-- Comments Section -->
    <div class="space-y-6">
        <h3 class="text-xl font-black text-gray-800 flex items-center gap-2">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
            </svg>
            Комментарии
        </h3>

        <div class="space-y-4">
            {#if $commentsQuery.data}
                {#each $commentsQuery.data as comment}
                    <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-50 flex gap-4">
                        <div class="w-10 h-10 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 font-bold text-lg shrink-0">
                            {comment.authorName?.[0]}
                        </div>
                        <div class="space-y-2 flex-grow min-w-0">
                            <div class="flex justify-between items-center">
                                <span class="font-bold text-indigo-600 text-sm">{comment.authorName}</span>
                                <span class="text-xs text-gray-400">{formatDate(comment.createdAt)}</span>
                            </div>
                            <p class="text-gray-700 leading-relaxed text-sm whitespace-pre-wrap">{comment.text}</p>

                            <!-- Comment Attachments -->
                            {#if comment.attachments?.length > 0}
                                <div class="flex flex-wrap gap-2 pt-2">
                                    {#each comment.attachments as att}
                                        <a href={api.tickets.downloadAttachmentUrl(att.id)} download={att.fileName}
                                           class="flex items-center gap-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 px-3 py-1.5 rounded-lg text-xs font-bold transition-colors">
                                            <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                                            </svg>
                                            {att.fileName} ({formatFileSize(att.fileSize)})
                                        </a>
                                    {/each}
                                </div>
                            {/if}
                        </div>
                    </div>
                {/each}
            {/if}

            <!-- New Comment -->
            <div class="bg-indigo-50/30 p-6 rounded-2xl border border-indigo-100">
                <textarea
                    bind:value={newComment}
                    placeholder="Напишите комментарий..."
                    class="w-full bg-white border border-indigo-100 rounded-xl p-4 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all h-32 resize-none"
                ></textarea>
                <div class="flex justify-end mt-4">
                    <button
                        on:click={() => $commentMutation.mutate(newComment)}
                        disabled={!newComment.trim() || $commentMutation.isPending}
                        class="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white px-6 py-2.5 rounded-xl font-bold text-sm transition-all shadow-lg shadow-indigo-100">
                        {$commentMutation.isPending ? 'Отправка...' : 'Отправить'}
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Resolution Modal -->
{#if showResolutionModal}
    <div class="fixed inset-0 bg-gray-900/60 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
        <div class="bg-white w-full max-w-lg rounded-2xl shadow-2xl p-8 space-y-6">
            <h2 class="text-2xl font-black text-gray-800">Закрытие заявки</h2>
            <div class="bg-blue-50 p-4 rounded-xl border border-blue-100 flex gap-3">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-blue-500 shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <p class="text-sm text-blue-700">Пожалуйста, опишите принятое решение для закрытия заявки.</p>
            </div>
            <textarea
                bind:value={resolutionText}
                class="w-full bg-gray-50 border border-gray-200 rounded-xl p-4 text-sm focus:ring-2 focus:ring-indigo-500 outline-none h-32 resize-none"
                placeholder="Текст решения..."></textarea>
            <div class="flex gap-4 justify-end">
                <button on:click={() => showResolutionModal = false} class="px-6 py-2 text-gray-500 font-bold hover:text-gray-700">Отмена</button>
                <button on:click={submitResolution} disabled={!resolutionText.trim()}
                        class="bg-red-500 hover:bg-red-600 disabled:opacity-50 text-white px-8 py-2.5 rounded-xl font-bold shadow-lg shadow-red-100 transition-all">
                    Закрыть заявку
                </button>
            </div>
        </div>
    </div>
{/if}

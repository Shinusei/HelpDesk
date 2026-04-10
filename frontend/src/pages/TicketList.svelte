<script>
    import { createQuery } from '@tanstack/svelte-query';
    import { Link } from "svelte-routing";
    import { api } from '../lib/api';
    import { formatDate, getStatusColor, getPriorityColor } from '../lib/utils';

    // Passed in from App.svelte context
    export let user = null;

    let sort = 'priorityScore';
    let dir = 'desc';
    let includeClosed = false;

    // Re-run the query reactively when sort/dir change
    $: ticketsQuery = createQuery({
        queryKey: ['tickets', includeClosed, sort, dir],
        queryFn: () => api.tickets.list(includeClosed, sort, dir),
        refetchInterval: 5000, // Refresh every 5 seconds
    });

    const statusMap = {
        'NEW': 'Новый',
        'IN_PROGRESS': 'В работе',
        'CLOSED': 'Закрыт'
    };

    const categoryMap = {
        'HARDWARE': 'Оборудование',
        'SOFTWARE': 'ПО',
        'NETWORK': 'Сеть',
        'ACCESS': 'Доступ',
        'OTHER': 'Прочее'
    };

    const importanceMap = {
        'LOW': 'Низкая',
        'MEDIUM': 'Средняя',
        'HIGH': 'Высокая',
        'CRITICAL': 'Критическая'
    };

    const sortOptions = [
        { value: 'priorityScore', label: 'Приоритет' },
        { value: 'createdAt',    label: 'Дата создания' },
        { value: 'title',        label: 'Название' },
        { value: 'status',       label: 'Статус' },
    ];

    // Regular users (EMPLOYEE / VIP / USER) should not see priority scores
    $: isRegularUser = user?.role === 'ROLE_EMPLOYEE' || user?.role === 'ROLE_VIP' || user?.role === 'ROLE_USER';

    function toggleSort(field) {
        if (sort === field) {
            dir = dir === 'desc' ? 'asc' : 'desc';
        } else {
            sort = field;
            dir = 'desc';
        }
    }
</script>

<div class="space-y-6">
    <!-- Header -->
    <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
            <h1 class="text-3xl font-black text-gray-900">Список заявок</h1>
            {#if $ticketsQuery.data}
                <p class="text-gray-500 font-medium mt-1">{$ticketsQuery.data.length} записей</p>
            {/if}
        </div>

        <div class="flex items-center gap-3 w-full sm:w-auto">
            <div class="flex items-center justify-between gap-3 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2.5 flex-1 sm:flex-none">
                <label for="include-closed" class="text-sm font-bold text-gray-700 whitespace-nowrap">Показывать закрытые</label>
                <input id="include-closed" type="checkbox" bind:checked={includeClosed} class="h-4 w-4 accent-indigo-600" />
            </div>
            <button on:click={() => $ticketsQuery.refetch()} class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors flex-shrink-0" title="Обновить">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
            </button>
        </div>
    </div>

    <!-- Sort Buttons -->
    <div class="flex flex-wrap gap-2">
        {#each sortOptions as opt}
            <button
                on:click={() => toggleSort(opt.value)}
                class="flex items-center gap-1.5 px-4 py-2 rounded-xl text-sm font-bold transition-all {sort === opt.value
                    ? 'bg-indigo-600 text-white'
                    : 'bg-gray-100 text-gray-500 hover:bg-gray-200'}"
            >
                {opt.label}
                {#if sort === opt.value}
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 transition-transform {dir === 'asc' ? 'rotate-180' : ''}" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7" />
                    </svg>
                {/if}
            </button>
        {/each}
    </div>

    <!-- Loading State -->
    {#if $ticketsQuery.isLoading}
        <div class="flex flex-col items-center justify-center py-24 bg-white rounded-3xl shadow-sm">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
            <p class="text-gray-500 font-medium">Загрузка данных...</p>
        </div>

    <!-- Error State -->
    {:else if $ticketsQuery.error}
        <div class="bg-red-50 border border-red-200 text-red-700 p-8 rounded-3xl text-center">
            <p class="font-bold text-lg">Ошибка при загрузке данных</p>
            <p class="text-sm opacity-80 mt-2">{$ticketsQuery.error.message}</p>
        </div>

    <!-- Empty State -->
    {:else if $ticketsQuery.data?.length === 0}
        <div class="text-center py-24 bg-white rounded-3xl shadow-sm border border-dashed border-gray-200">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 mx-auto text-gray-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
            <p class="text-gray-400 text-xl font-bold">Заявок пока нет</p>
            <p class="text-gray-300 mt-2">Создайте первую заявку с помощью кнопки «Создать»</p>
        </div>

    <!-- Ticket List -->
    {:else if $ticketsQuery.data}
        <div class="grid gap-3">
            {#each $ticketsQuery.data as ticket}
                <Link to="/tickets/{ticket.id}" class="block group">
                    <div class="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 group-hover:border-indigo-200 group-hover:shadow-md transition-all duration-200">
                        <div class="flex flex-col sm:flex-row justify-between items-start gap-3">

                            <!-- Left: Title + Meta -->
                            <div class="min-w-0 flex-grow">
                                <div class="flex items-center gap-2 flex-wrap mb-1">
                                    <span class="text-xs font-bold text-gray-300">#{ticket.id}</span>
                                    <span class="px-2.5 py-0.5 rounded-lg text-xs font-bold uppercase tracking-wider border {getStatusColor(ticket.status)}">
                                        {statusMap[ticket.status] || ticket.status}
                                    </span>
                                    {#if ticket.category}
                                        <span class="px-2 py-0.5 rounded-lg text-xs font-bold bg-gray-100 text-gray-500">
                                            {categoryMap[ticket.category] || ticket.category}
                                        </span>
                                    {/if}
                                </div>
                                <h3 class="text-base font-black text-gray-900 group-hover:text-indigo-600 transition-colors truncate">{ticket.title}</h3>
                                <p class="text-xs text-gray-400 mt-1">
                                    {formatDate(ticket.createdAt)} &nbsp;·&nbsp; <span class="text-gray-500 font-medium">{ticket.creatorName}</span>
                                    {#if ticket.executorName && ticket.executorName !== 'Не назначен'}
                                        &nbsp;·&nbsp; Исполнитель: <span class="text-indigo-500 font-medium">{ticket.executorName}</span>
                                    {/if}
                                </p>
                            </div>

                            <!-- Right: Priority (hidden for regular users) -->
                            {#if !isRegularUser}
                                <div class="shrink-0 text-right">
                                    <span class="block text-[10px] uppercase tracking-widest font-black text-gray-400 mb-0.5">Приоритет</span>
                                    <span class="text-lg font-black {getPriorityColor(ticket.priorityScore)}">
                                        {ticket.priorityScore?.toFixed(1) || '0.0'}
                                    </span>
                                </div>
                            {/if}
                        </div>

                        <!-- Footer Row -->
                        <div class="flex flex-wrap gap-3 mt-3 pt-3 border-t border-gray-50">
                            {#if ticket.importance}
                                <span class="text-xs text-gray-400"><span class="font-bold text-gray-600">Важность:</span> {importanceMap[ticket.importance] || ticket.importance}</span>
                            {/if}
                            {#if ticket.urgency}
                                <span class="text-xs text-gray-400"><span class="font-bold text-gray-600">Срочность:</span> {importanceMap[ticket.urgency] || ticket.urgency}</span>
                            {/if}
                            {#if ticket.slaDeadline}
                                <span class="text-xs {new Date(ticket.slaDeadline) < new Date() && ticket.status !== 'CLOSED' ? 'text-red-500 font-bold' : 'text-gray-400'}">
                                    SLA: {formatDate(ticket.slaDeadline)}
                                </span>
                            {/if}
                        </div>
                    </div>
                </Link>
            {/each}
        </div>
    {/if}
</div>

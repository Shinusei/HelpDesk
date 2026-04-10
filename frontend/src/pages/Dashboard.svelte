<script>
    import { createQuery } from '@tanstack/svelte-query';
    import { Link } from 'svelte-routing';
    import { api } from '../lib/api';

    export let user = null;

    const statsQuery = createQuery({
        queryKey: ['admin-stats'],
        queryFn: () => api.admin.dashboard.stats(),
        refetchInterval: 30000 // Refresh every 30s
    });

    const statusMap = {
        'NEW': 'Новые',
        'IN_PROGRESS': 'В работе',
        'CLOSED': 'Закрытые'
    };

    const categoryMap = {
        'HARDWARE': 'Оборудование',
        'SOFTWARE': 'ПО',
        'NETWORK': 'Сеть',
        'ACCESS': 'Доступ',
        'OTHER': 'Прочее'
    };
</script>

<div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex justify-between items-center">
        <div>
            <h1 class="text-3xl font-black text-gray-900">Дашборд</h1>
            <p class="text-gray-500 font-medium">Общая статистика по тикетам</p>
        </div>
        <div class="flex gap-2">
            <button on:click={() => $statsQuery.refetch()} class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
            </button>
        </div>
    </div>

    {#if $statsQuery.isLoading}
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {#each Array(4) as _}
                <div class="bg-white p-8 rounded-3xl shadow-sm animate-pulse space-y-4">
                    <div class="h-4 bg-gray-100 rounded w-1/2"></div>
                    <div class="h-8 bg-gray-200 rounded w-1/4"></div>
                </div>
            {/each}
        </div>
    {:else if $statsQuery.data}
        <!-- Summary Cards -->
        <div class="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
            <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 space-y-2">
                <span class="text-xs font-black text-gray-400 uppercase tracking-widest">Всего заявок</span>
                <p class="text-4xl font-black text-indigo-600">{$statsQuery.data.totalTickets}</p>
            </div>
            <div class="bg-indigo-600 p-8 rounded-3xl shadow-xl shadow-indigo-100 space-y-2 text-white">
                <span class="text-xs font-black opacity-60 uppercase tracking-widest">SLA Просрочено</span>
                <p class="text-4xl font-black">{$statsQuery.data.overdueTickets}</p>
            </div>
            <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 space-y-2">
                <span class="text-xs font-black text-gray-400 uppercase tracking-widest">Ср. время решения</span>
                <p class="text-4xl font-black text-gray-800">{$statsQuery.data.avgResolutionTimeHours?.toFixed(1) || '0'} ч.</p>
            </div>
            <div class="bg-green-500 p-8 rounded-3xl shadow-xl shadow-green-100 space-y-2 text-white">
                <span class="text-xs font-black opacity-60 uppercase tracking-widest">Закрыто</span>
                <p class="text-4xl font-black">{$statsQuery.data.closedTickets}</p>
            </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <!-- Status Distribution -->
            <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 space-y-6">
                <h3 class="text-xl font-black text-gray-800">По статусу</h3>
                <div class="space-y-4">
                    {#each Object.entries($statsQuery.data.statusDistribution || {}) as [status, count]}
                        <div class="space-y-1">
                            <div class="flex justify-between text-sm font-bold">
                                <span>{statusMap[status] || status}</span>
                                <span>{count}</span>
                            </div>
                            <div class="w-full bg-gray-100 rounded-full h-2">
                                <div class="bg-indigo-500 h-2 rounded-full" style="width: {(count / $statsQuery.data.totalTickets * 100).toFixed(0)}%"></div>
                            </div>
                        </div>
                    {/each}
                </div>
            </div>

            <!-- Category Distribution -->
            <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 space-y-6">
                <h3 class="text-xl font-black text-gray-800">По категориям</h3>
                <div class="space-y-4">
                    {#each Object.entries($statsQuery.data.categoryDistribution || {}) as [cat, count]}
                        <div class="space-y-1">
                            <div class="flex justify-between text-sm font-bold">
                                <span>{categoryMap[cat] || cat}</span>
                                <span>{count}</span>
                            </div>
                            <div class="w-full bg-gray-100 rounded-full h-2">
                                <div class="bg-orange-400 h-2 rounded-full" style="width: {(count / $statsQuery.data.totalTickets * 100).toFixed(0)}%"></div>
                            </div>
                        </div>
                    {/each}
                </div>
            </div>
        </div>
        {#if user?.role === 'ADMIN' || user?.role === 'ROLE_ADMIN'}
            <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                <h3 class="text-xl font-black text-gray-800 mb-6">Быстрые действия</h3>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    <Link to="/admin/users"
                          class="flex items-center gap-4 p-5 rounded-2xl border-2 border-indigo-100 bg-indigo-50/50 hover:bg-indigo-50 hover:border-indigo-300 transition-all group">
                        <div class="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center group-hover:bg-indigo-200 transition-colors shrink-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                            </svg>
                        </div>
                        <div>
                            <p class="font-black text-gray-900 group-hover:text-indigo-700 transition-colors">Управление пользователями</p>
                            <p class="text-xs text-gray-500 mt-0.5">Создание, редактирование, удаление</p>
                        </div>
                    </Link>

                    <Link to="/admin/priority"
                          class="flex items-center gap-4 p-5 rounded-2xl border-2 border-orange-100 bg-orange-50/50 hover:bg-orange-50 hover:border-orange-300 transition-all group">
                        <div class="w-12 h-12 bg-orange-100 rounded-xl flex items-center justify-center group-hover:bg-orange-200 transition-colors shrink-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-orange-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                            </svg>
                        </div>
                        <div>
                            <p class="font-black text-gray-900 group-hover:text-orange-700 transition-colors">Настройки приоритетов</p>
                            <p class="text-xs text-gray-500 mt-0.5">Веса параметров и SLA</p>
                        </div>
                    </Link>

                    <Link to="/tickets/new"
                          class="flex items-center gap-4 p-5 rounded-2xl border-2 border-green-100 bg-green-50/50 hover:bg-green-50 hover:border-green-300 transition-all group">
                        <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center group-hover:bg-green-200 transition-colors shrink-0">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                            </svg>
                        </div>
                        <div>
                            <p class="font-black text-gray-900 group-hover:text-green-700 transition-colors">Создать заявку</p>
                            <p class="text-xs text-gray-500 mt-0.5">Новое обращение в поддержку</p>
                        </div>
                    </Link>
                </div>
            </div>
        {/if}
    {/if}
</div>

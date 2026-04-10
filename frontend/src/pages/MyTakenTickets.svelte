<script>
  import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
  import { Link } from 'svelte-routing';
  import { api } from '../lib/api';
  import { formatDate, getStatusColor, getPriorityColor } from '../lib/utils';

  export let user = null;

  let includeClosed = false;
  let sort = 'priorityScore';
  let dir = 'desc';
  const queryClient = useQueryClient();
  let lastAutoAssignTime = 0;

  $: ticketsQuery = createQuery({
    queryKey: ['tickets', 'assigned-to-me', includeClosed, sort, dir],
    queryFn: () => api.tickets.assignedToMe(includeClosed, sort, dir),
    enabled: user?.role === 'ROLE_IT_SUPPORT' || user?.role === 'ROLE_ADMIN',
    refetchInterval: 5000, // Refresh every 5 seconds
  });

  const autoAssignMutation = createMutation({
    mutationFn: () => api.tickets.autoAssign(),
    onSuccess: () => {
      lastAutoAssignTime = Date.now();
      queryClient.invalidateQueries(['tickets', 'assigned-to-me', includeClosed, sort, dir]);
    }
  });

  // Auto-assign when no tickets (with cooldown to prevent rapid-fire calls)
  $: if ($ticketsQuery.isSuccess && $ticketsQuery.data && $ticketsQuery.data.length === 0 && !$autoAssignMutation.isPending && Date.now() - lastAutoAssignTime > 2000) {
    $autoAssignMutation.mutate();
  }

  const statusMap = { NEW: 'Новый', IN_PROGRESS: 'В работе', CLOSED: 'Закрыт' };
  const categoryMap = { HARDWARE: 'Оборудование', SOFTWARE: 'ПО', NETWORK: 'Сеть', ACCESS: 'Доступ', OTHER: 'Прочее' };

  const sortOptions = [
    { value: 'priorityScore', label: 'Приоритет' },
    { value: 'createdAt', label: 'Дата создания' },
    { value: 'title', label: 'Название' },
    { value: 'status', label: 'Статус' },
  ];

  function toggleSort(field) {
    if (sort === field) dir = dir === 'desc' ? 'asc' : 'desc';
    else { sort = field; dir = 'desc'; }
  }
</script>

<div class="space-y-6">
  <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 space-y-4">
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h1 class="text-3xl font-black text-gray-900">В работе</h1>
        <p class="text-gray-500 font-medium mt-1">Заявки, назначенные на вас</p>
      </div>
      <button on:click={() => $ticketsQuery.refetch()} class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors flex-shrink-0" title="Обновить">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      </button>
    </div>

    <div class="flex items-center justify-between gap-3 bg-gray-50 border border-gray-200 rounded-xl px-4 py-2.5">
      <label for="include-closed" class="text-sm font-bold text-gray-700">Показывать закрытые</label>
      <input id="include-closed" type="checkbox" bind:checked={includeClosed} class="h-4 w-4 accent-indigo-600" />
    </div>
  </div>

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

  {#if $ticketsQuery.isLoading}
    <div class="flex flex-col items-center justify-center py-24 bg-white rounded-3xl shadow-sm">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
      <p class="text-gray-500 font-medium">Загрузка данных...</p>
    </div>
  {:else if $ticketsQuery.error}
    <div class="bg-red-50 border border-red-200 text-red-700 p-8 rounded-3xl text-center">
      <p class="font-bold text-lg">Ошибка при загрузке данных</p>
      <p class="text-sm opacity-80 mt-2">{$ticketsQuery.error.message}</p>
    </div>
  {:else if $ticketsQuery.data?.length === 0}
    <div class="flex flex-col items-center justify-center py-24 bg-white rounded-3xl shadow-sm border border-dashed border-gray-200 space-y-4">
      <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <p class="text-gray-400 text-lg font-bold">У вас нет назначенных заявок</p>
      {#if $autoAssignMutation.isPending}
        <p class="text-indigo-600 font-medium text-sm animate-pulse">Поиск новых заявок...</p>
      {/if}
    </div>
  {:else if $ticketsQuery.data}
    <div class="grid gap-3">
      {#each $ticketsQuery.data as ticket}
        <Link to="/tickets/{ticket.id}" class="block group">
          <div class="bg-white p-5 rounded-2xl shadow-sm border border-gray-100 group-hover:border-indigo-200 group-hover:shadow-md transition-all duration-200">
            <div class="flex flex-col sm:flex-row justify-between items-start gap-3">
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
                </p>
              </div>

              <div class="shrink-0 text-right">
                <span class="block text-[10px] uppercase tracking-widest font-black text-gray-400 mb-0.5">Приоритет</span>
                <span class="text-lg font-black {getPriorityColor(ticket.priorityScore)}">
                  {ticket.priorityScore?.toFixed(1) || '0.0'}
                </span>
              </div>
            </div>
          </div>
        </Link>
      {/each}
    </div>
  {/if}
</div>


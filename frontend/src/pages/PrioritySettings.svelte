<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { api } from '../lib/api';

    const queryClient = useQueryClient();

    const weightsQuery = createQuery({
        queryKey: ['admin-priority-weights'],
        queryFn: () => api.admin.priority.list()
    });

    const updateMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.priority.update(id, data),
        onSuccess: () => queryClient.invalidateQueries(['admin-priority-weights'])
    });

    let editingId = null;
    let editData = { weight: 0, slaHours: 0 };

    function startEdit(weight) {
        editingId = weight.id;
        editData = { weight: weight.weight, slaHours: weight.slaHours };
    }

    function handleSave() {
        $updateMutation.mutate({ id: editingId, data: editData });
        editingId = null;
    }

    const paramNames = {
        'URGENCY': 'Срочность',
        'IMPORTANCE': 'Важность',
        'IMPACT': 'Влияние'
    };
</script>

<div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
        <h1 class="text-3xl font-black text-gray-900">Настройки приоритетов</h1>
        <p class="text-gray-500 font-medium mt-2">Веса параметров и SLA (в часах)</p>
    </div>

    {#if $weightsQuery.isLoading}
        <div class="flex justify-center py-20 bg-white rounded-3xl shadow-sm">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
    {:else if $weightsQuery.data}
        <div class="grid gap-6">
            {#each $weightsQuery.data as weight}
                <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col md:flex-row justify-between items-center gap-6">
                    <div class="flex items-center gap-6 grow">
                        <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center font-bold">
                            {weight.id}
                        </div>
                        <div>
                            <h3 class="text-lg font-black text-gray-800">{paramNames[weight.parameter] || weight.parameter}</h3>
                            <p class="text-sm font-bold text-gray-400">{weight.valueName || 'Общий вес'}</p>
                        </div>
                    </div>

                    {#if editingId === weight.id}
                        <div class="flex flex-wrap items-center gap-4 bg-indigo-50/50 p-4 rounded-2xl border border-indigo-100">
                            <div class="space-y-1">
                                <label for="weight-{weight.id}" class="text-[10px] font-black text-indigo-400 uppercase tracking-widest pl-1">Вес</label>
                                <input id="weight-{weight.id}" type="number" step="0.1" bind:value={editData.weight} class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                            </div>
                            <div class="space-y-1">
                                <label for="sla-{weight.id}" class="text-[10px] font-black text-indigo-400 uppercase tracking-widest pl-1">SLA (ч)</label>
                                <input id="sla-{weight.id}" type="number" bind:value={editData.slaHours} class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                            </div>
                            <div class="flex gap-2 pt-4">
                                <button on:click={() => editingId = null} class="p-2 text-gray-400 hover:text-gray-600 transition-colors">
                                    Отмена
                                </button>
                                <button on:click={handleSave} class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-black shadow-lg shadow-indigo-100">
                                    ОК
                                </button>
                            </div>
                        </div>
                    {:else}
                        <div class="flex gap-12 text-center">
                            <div>
                                <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Вес</span>
                                <span class="text-xl font-black text-indigo-600">{weight.weight}</span>
                            </div>
                            <div>
                                <span class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">SLA</span>
                                <span class="text-xl font-black text-gray-800">{weight.slaHours} ч.</span>
                            </div>
                        </div>
                        <button on:click={() => startEdit(weight)} class="p-3 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                        </button>
                    {/if}
                </div>
            {/each}
        </div>
    {/if}
</div>

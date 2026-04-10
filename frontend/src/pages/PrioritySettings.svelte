<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { api } from '../lib/api';

    const queryClient = useQueryClient();

    const weightsQuery = createQuery({
        queryKey: ['admin-priority-weights'],
        queryFn: () => api.admin.priority.list(),
    });

    const paramValuesQuery = createQuery({
        queryKey: ['admin-parameter-values'],
        queryFn: () => api.admin.parameterValues.listAll(),
    });

    const updateMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.priority.update(id, data),
        onSuccess: () => queryClient.invalidateQueries(['admin-priority-weights'])
    });

    const updateValueMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.parameterValues.update(id, data),
        onSuccess: () => queryClient.invalidateQueries(['admin-parameter-values'])
    });

    const resetMutation = createMutation({
        mutationFn: () => api.admin.priority.reset(),
        onSuccess: () => queryClient.invalidateQueries(['admin-priority-weights'])
    });

    let editingId = null;
    let editingValueId = null;
    let editData = { weightValue: 0, description: '' };
    let editValueData = { weightValue: 0 };
    let expandedParams = new Set();

    function toggleParam(param) {
        if (expandedParams.has(param)) {
            expandedParams.delete(param);
        } else {
            expandedParams.add(param);
        }
        expandedParams = expandedParams;
    }

    function startEdit(weight) {
        editingId = weight.id;
        editData = { weightValue: weight.weightValue, description: weight.description || '' };
    }

    function handleSave() {
        $updateMutation.mutate({ id: editingId, data: editData });
        editingId = null;
    }

    function startEditValue(value) {
        editingValueId = value.id;
        editValueData = { weightValue: value.weightValue };
    }

    function handleSaveValue() {
        $updateValueMutation.mutate({ id: editingValueId, data: editValueData });
        editingValueId = null;
    }

    function handleReset() {
        if (confirm('Вы уверены, что хотите сбросить все параметры на базовые значения?')) {
            $resetMutation.mutate();
            editingId = null;
        }
    }

    function groupByParam(values) {
        const grouped = {};
        values.forEach(v => {
            if (!grouped[v.paramName]) {
                grouped[v.paramName] = [];
            }
            grouped[v.paramName].push(v);
        });
        return grouped;
    }

    function getParamWeight(paramName) {
        return $weightsQuery.data?.find(w => w.paramName === paramName);
    }

    function getParamDisplay(paramName) {
        const displays = {
            URGENCY: 'Срочность',
            IMPORTANCE: 'Важность',
            IMPACT: 'Влияние',
            CATEGORY: 'Категория',
            CREATOR_ROLE: 'Роль заявителя',
            NEWER_UNRESOLVED_TICKETS: 'Кол-во новых заявок',
            WAITING_HOURS: 'Часы ожидания',
        };
        return displays[paramName] || paramName;
    }

    $: isLoading = $weightsQuery.isLoading || $paramValuesQuery.isLoading;
</script>

<div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl border border-gray-100 flex justify-between items-start">
        <div>
            <h1 class="text-3xl font-black text-gray-900">Настройки приоритетов</h1>
            <p class="text-gray-500 font-medium mt-2">Веса параметров и их значений</p>
        </div>
        <button on:click={handleReset} disabled={$resetMutation.isPending}
                class="bg-red-500 hover:bg-red-600 disabled:opacity-50 text-white px-5 py-2.5 rounded-xl text-sm font-black transition-colors whitespace-nowrap">
            {$resetMutation.isPending ? 'Сброс...' : 'Сброс на базовые'}
        </button>
    </div>

    {#if isLoading}
        <div class="flex justify-center py-20 bg-white rounded-3xl">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
    {:else if $weightsQuery.data && $paramValuesQuery.data}
        <div class="space-y-4">
            {#each Object.entries(groupByParam($paramValuesQuery.data)) as [paramName, values] (paramName)}
                <div class="bg-white rounded-3xl border border-gray-100 overflow-hidden">
                    <!-- Header -->
                    <button on:click={() => toggleParam(paramName)}
                            class="w-full p-6 flex justify-between items-center hover:bg-gray-50 transition-colors"
                    >
                        <div class="text-left flex-1">
                            <h2 class="text-lg font-black text-gray-900">{getParamDisplay(paramName)}</h2>
                            <p class="text-xs text-gray-400 mt-1">{paramName}</p>
                        </div>
                        <div class="flex items-center gap-6">
                            {#if editingId === getParamWeight(paramName)?.id}
                                <div class="flex items-end gap-2">
                                    <div class="space-y-1">
                                        <label class="text-[10px] font-black text-indigo-400 uppercase tracking-widest">Вес параметра</label>
                                        <input type="number" step="0.1" bind:value={editData.weightValue} 
                                               class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                                    </div>
                                    <button on:click|stopPropagation={() => editingId = null} 
                                            class="p-2 text-gray-400 hover:text-gray-600 transition-colors">
                                        Отмена
                                    </button>
                                    <button on:click|stopPropagation={handleSave}
                                            class="bg-indigo-600 text-white px-3 py-2 rounded-xl text-sm font-black">
                                        ОК
                                    </button>
                                </div>
                            {:else}
                                <div class="flex items-center gap-4">
                                    <span class="text-sm font-black text-gray-500">Вес:</span>
                                    <span class="text-2xl font-black text-indigo-600">{getParamWeight(paramName)?.weightValue || 0}</span>
                                    <button on:click|stopPropagation={() => startEdit(getParamWeight(paramName))}
                                            class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                        </svg>
                                    </button>
                                </div>
                            {/if}
                            <div class="text-indigo-400 transition-transform {expandedParams.has(paramName) ? 'rotate-180' : ''}">
                                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                </svg>
                            </div>
                        </div>
                    </button>

                    <!-- Values -->
                    {#if expandedParams.has(paramName)}
                        <div class="border-t border-gray-100 p-6 space-y-3 bg-gray-50">
                            {#each values as value (value.id)}
                                <div class="flex items-center justify-between gap-6 p-4 bg-white rounded-2xl border border-gray-100">
                                    <div class="flex-1">
                                        <h3 class="text-sm font-bold text-gray-900">{value.displayName || value.valueName}</h3>
                                        <p class="text-xs text-gray-400 mt-1">({value.valueName})</p>
                                    </div>
                                    
                                    {#if editingValueId === value.id}
                                        <div class="flex items-end gap-2">
                                            <div class="space-y-1">
                                                <label class="text-[10px] font-black text-indigo-400 uppercase tracking-widest">Вес значения</label>
                                                <input type="number" step="0.1" bind:value={editValueData.weightValue} 
                                                       class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                                            </div>
                                            <button on:click={() => editingValueId = null} 
                                                    class="p-2 text-gray-400 hover:text-gray-600 transition-colors">
                                                Отмена
                                            </button>
                                            <button on:click={handleSaveValue}
                                                    class="bg-indigo-600 text-white px-3 py-2 rounded-xl text-sm font-black">
                                                ОК
                                            </button>
                                        </div>
                                    {:else}
                                        <div class="flex items-center gap-4">
                                            <span class="text-lg font-black text-indigo-600 min-w-[3rem] text-right">{value.weightValue}</span>
                                            <button on:click={() => startEditValue(value)}
                                                    class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                                </svg>
                                            </button>
                                        </div>
                                    {/if}
                                </div>
                            {/each}
                        </div>
                    {/if}
                </div>
            {/each}
        </div>
    {/if}
</div>

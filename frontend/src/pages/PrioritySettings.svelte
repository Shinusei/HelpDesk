<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { api } from '../lib/api';

    const queryClient = useQueryClient();

    // --- Queries ---
    const weightsQuery = createQuery({
        queryKey: ['admin-priority-weights'],
        queryFn: () => api.admin.priority.list(),
    });

    const paramValuesQuery = createQuery({
        queryKey: ['admin-parameter-values'],
        queryFn: () => api.admin.parameterValues.listAll(),
    });

    const dynamicFiltersQuery = createQuery({
        queryKey: ['admin-dynamic-filters'],
        queryFn: () => api.admin.dynamicFilters.list(),
    });

    // --- Mutations (System Parameters) ---
    const updateWeightMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.priority.update(id, data),
        onSuccess: (newData) => {
            queryClient.setQueryData(['admin-priority-weights'], (old) => {
                if (!old) return [newData];
                return old.map(w => w.id === newData.id ? newData : w);
            });
            queryClient.invalidateQueries({ queryKey: ['admin-priority-weights'] });
        }
    });

    const updateParamValueMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.parameterValues.update(id, data),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-parameter-values'] })
    });

    const resetWeightsMutation = createMutation({
        mutationFn: () => api.admin.priority.reset(),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-priority-weights'] })
    });

    // --- Mutations (Dynamic Filters) ---
    const addFilterMutation = createMutation({
        mutationFn: (data) => api.admin.dynamicFilters.create(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] });
            newFilter = { name: '', displayName: '', weight: 1.0 };
        },
        onError: (err) => alert('Ошибка при создании параметра: ' + err.message)
    });

    const updateFilterMutation = createMutation({
        mutationFn: ({ id, data }) => api.admin.dynamicFilters.update(id, data),
        onSuccess: (newData) => {
            queryClient.setQueryData(['admin-dynamic-filters'], (old) => {
                if (!old) return [newData];
                return old.map(f => f.id === newData.id ? newData : f);
            });
            queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] });
        }
    });

    const deleteFilterMutation = createMutation({
        mutationFn: (id) => api.admin.dynamicFilters.delete(id),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] })
    });

    const addFilterValueMutation = createMutation({
        mutationFn: ({ filterId, data }) => api.admin.dynamicFilters.addValue(filterId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] });
            newValue = { valueName: '', displayName: '', weightValue: 1.0 };
        },
        onError: (err) => alert('Ошибка при добавлении значения: ' + err.message)
    });

    const updateFilterValueMutation = createMutation({
        mutationFn: ({ valueId, data }) => api.admin.dynamicFilters.updateValue(valueId, data),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] })
    });

    const deleteFilterValueMutation = createMutation({
        mutationFn: (valueId) => api.admin.dynamicFilters.deleteValue(valueId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin-dynamic-filters'] })
    });

    // --- State ---
    let editingId = null; // for PriorityWeight (System)
    let editingValueId = null; // for ParameterValueWeight (System) or DynamicFilterValue (Custom)
    let editingFilterId = null; // for DynamicFilter (Custom)
    
    let editData = { weightValue: 0, description: '' };
    let editValueData = { weightValue: 0, displayName: '' };
    let expandedParams = new Set();
    
    let newFilter = { name: '', displayName: '', weight: 1.0 };
    let newValue = { valueName: '', displayName: '', weightValue: 1.0 };

    // --- Handlers ---
    function toggleParam(param) {
        if (expandedParams.has(param)) expandedParams.delete(param);
        else expandedParams.add(param);
        expandedParams = expandedParams;
    }

    // System Weight Handlers
    function startEdit(weight) {
        editingId = weight.id;
        editData = { weightValue: weight.weightValue, description: weight.description || '' };
    }
    function handleSave() {
        $updateWeightMutation.mutate({ id: editingId, data: editData });
        editingId = null;
    }
    
    function toggleSystemParam(weight) {
        const nextActive = weight.active === false ? true : false;
        $updateWeightMutation.mutate({ 
            id: weight.id, 
            data: { active: nextActive } 
        });
    }

    // System Value Handlers
    function startEditValue(value) {
        editingValueId = value.id;
        editValueData = { weightValue: value.weightValue, displayName: value.displayName };
    }
    function handleSaveValue() {
        $updateParamValueMutation.mutate({ id: editingValueId, data: editValueData });
        editingValueId = null;
    }

    // Dynamic Filter Handlers
    function handleCreateFilter() {
        if (!newFilter.name || !newFilter.displayName) return;
        // Validate technical name
        if (!/^[A-Z0-9_]+$/.test(newFilter.name)) {
            alert('Техническое имя должно содержать только латинские заглавные буквы, цифры и подчеркивание (например, OFFICE_LOCATION)');
            return;
        }
        $addFilterMutation.mutate(newFilter);
    }
    function handleDeleteFilter(id) {
        if (confirm('Удалить этот фильтр?')) $deleteFilterMutation.mutate(id);
    }
    function startEditFilter(filter) {
        editingFilterId = filter.id;
        editData = { weightValue: filter.weight, displayName: filter.displayName };
    }
    function handleSaveFilter() {
        $updateFilterMutation.mutate({ id: editingFilterId, data: { displayName: editData.displayName, weight: editData.weightValue } });
        editingFilterId = null;
    }
    
    function toggleFilter(filter) {
        const current = filter.isActive !== false;
        $updateFilterMutation.mutate({ 
            id: filter.id, 
            data: { isActive: !current } 
        });
    }

    // Dynamic Value Handlers
    function handleAddFilterValue(filterId) {
        if (!newValue.valueName || !newValue.displayName) return;
        // Validate technical value name
        if (!/^[A-Z0-9_]+$/.test(newValue.valueName)) {
            alert('Техническое имя значения должно содержать только латинские заглавные буквы, цифры и подчеркивание');
            return;
        }
        $addFilterValueMutation.mutate({ filterId, data: newValue });
    }
    function handleDeleteFilterValue(valueId) {
        if (confirm('Удалить значение?')) $deleteFilterValueMutation.mutate(valueId);
    }
    function startEditFilterValue(val) {
        editingValueId = val.id;
        editValueData = { weightValue: val.weightValue, displayName: val.displayName };
    }
    function handleSaveFilterValue() {
        $updateFilterValueMutation.mutate({ valueId: editingValueId, data: { displayName: editValueData.displayName, weightValue: editValueData.weightValue } });
        editingValueId = null;
    }

    function handleReset() {
        if (confirm('Вы уверены, что хотите сбросить все системные параметры?')) {
            $resetWeightsMutation.mutate();
            editingId = null;
        }
    }

    // --- Helpers ---
    function groupByParam(values) {
        if (!values) return {};
        const grouped = {};
        values.forEach(v => {
            if (!grouped[v.paramName]) grouped[v.paramName] = [];
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

    $: isLoading = $weightsQuery.isLoading || $paramValuesQuery.isLoading || $dynamicFiltersQuery.isLoading;
</script>

<div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl border border-gray-100 flex justify-between items-start">
        <div>
            <h1 class="text-3xl font-black text-gray-900">Настройки приоритетов</h1>
            <p class="text-gray-500 font-medium mt-2">Веса параметров и их значений</p>
        </div>
        <button on:click={handleReset} disabled={$resetWeightsMutation.isPending}
                class="bg-red-500 hover:bg-red-600 disabled:opacity-50 text-white px-5 py-2.5 rounded-xl text-sm font-black transition-colors whitespace-nowrap">
            {$resetWeightsMutation.isPending ? 'Сброс...' : 'Сброс на базовые'}
        </button>
    </div>
    {#if isLoading}
        <div class="flex justify-center py-20 bg-white rounded-3xl">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
    {:else if $weightsQuery.data && $paramValuesQuery.data}
        <div class="space-y-4">
            <!-- System Parameters -->
            {#key $weightsQuery.data}
                {#each Object.entries(groupByParam($paramValuesQuery.data)) as [paramName, values] (paramName)}
                    {@const weight = getParamWeight(paramName)}
                    <div class="bg-white rounded-3xl border border-gray-100 overflow-hidden shadow-sm">
                        <!-- Header -->
                        <button on:click={() => toggleParam(paramName)}
                                class="w-full p-6 flex justify-between items-center hover:bg-gray-50 transition-colors"
                        >
                            <div class="flex items-center gap-4 flex-1">
                                <button on:click|stopPropagation={() => toggleSystemParam(weight)}
                                        title={weight?.active !== false ? 'Выключить' : 'Включить'}
                                        class="w-10 h-5 rounded-full transition-colors relative shrink-0 {weight?.active !== false ? 'bg-indigo-600' : 'bg-gray-300'}">
                                    <div class="absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform {weight?.active !== false ? 'translate-x-5' : ''}"></div>
                                </button>
                                <div class="text-left flex-1">
                                    <h2 class="text-lg font-black text-gray-900 {weight?.active !== false ? '' : 'text-gray-400 line-through'}">{getParamDisplay(paramName)}</h2>
                                    <p class="text-xs text-gray-400 mt-1">{paramName} <span class="ml-2 px-1.5 py-0.5 bg-gray-100 text-gray-400 rounded text-[10px] uppercase font-black">Системный</span></p>
                                </div>
                            </div>
                            <div class="flex items-center gap-6">
                                {#if editingId === weight?.id}
                                    <div class="flex items-end gap-2" on:click|stopPropagation={() => {}} role="none">
                                        <div class="space-y-1 text-left">
                                            <label for="edit-weight-{paramName}" class="text-[10px] font-black text-indigo-400 uppercase tracking-widest pl-1">Вес</label>
                                            <input id="edit-weight-{paramName}" type="number" step="0.1" bind:value={editData.weightValue} 
                                                   class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                                        </div>
                                        <button on:click={() => editingId = null} class="p-2 text-gray-400 hover:text-gray-600 transition-colors">✕</button>
                                        <button on:click={handleSave} disabled={$updateWeightMutation.isPending}
                                                class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-black shadow-lg shadow-indigo-200 disabled:opacity-50">
                                            {$updateWeightMutation.isPending ? '...' : 'ОК'}
                                        </button>
                                    </div>
                                {:else}
                                    <div class="flex items-center gap-4">
                                        <span class="text-sm font-black text-gray-500">Вес:</span>
                                        <span class="text-2xl font-black text-indigo-600">{weight?.weightValue || 0}</span>
                                        <button on:click|stopPropagation={() => startEdit(weight)}
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
                        <div class="border-t border-gray-100 p-6 space-y-3 bg-gray-50/50">
                            {#each values as value (value.id)}
                                <div class="flex items-center justify-between gap-6 p-4 bg-white rounded-2xl border border-gray-100 group">
                                    <div class="flex-1">
                                        <h3 class="text-sm font-bold text-gray-900">{value.displayName || value.valueName}</h3>
                                        <p class="text-[10px] text-gray-400 uppercase tracking-tighter mt-0.5">Значение: {value.valueName}</p>
                                    </div>
                                    
                                    {#if editingValueId === value.id}
                                        <div class="flex items-end gap-2">
                                            <div class="space-y-1">
                                                <input type="number" step="0.1" bind:value={editValueData.weightValue} 
                                                       class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                                            </div>
                                            <button on:click={() => editingValueId = null} class="p-2 text-gray-400 hover:text-gray-600 transition-colors">✕</button>
                                            <button on:click={handleSaveValue} class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-black shadow-lg shadow-indigo-200">ОК</button>
                                        </div>
                                    {:else}
                                        <div class="flex items-center gap-4">
                                            <span class="text-lg font-black text-indigo-600">{value.weightValue}</span>
                                            <button on:click={() => startEditValue(value)}
                                                    class="p-2 text-indigo-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all opacity-0 group-hover:opacity-100">
                                                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
        {/key}

            <!-- Custom (Dynamic) Filters -->
            {#if $dynamicFiltersQuery.data}
                {#key $dynamicFiltersQuery.data}
                    {#each $dynamicFiltersQuery.data as filter (filter.id)}
                        <div class="bg-white rounded-3xl border border-gray-100 overflow-hidden shadow-sm">
                        <!-- Header -->
                        <button on:click={() => toggleParam('custom-' + filter.id)}
                                class="w-full p-6 flex justify-between items-center hover:bg-gray-50 transition-colors"
                        >
                        <div class="flex items-center gap-4 flex-1">
                            <button on:click|stopPropagation={() => toggleFilter(filter)}
                                    title={filter.isActive !== false ? 'Выключить' : 'Включить'}
                                    class="w-10 h-5 rounded-full transition-colors relative shrink-0 {filter.isActive !== false ? 'bg-indigo-600' : 'bg-gray-300'}">
                                <div class="absolute top-0.5 left-0.5 w-4 h-4 bg-white rounded-full transition-transform {filter.isActive !== false ? 'translate-x-5' : ''}"></div>
                            </button>
                            <div class="text-left flex-1">
                                {#if editingFilterId === filter.id}
                                    <div class="flex gap-2" on:click|stopPropagation={() => {}} role="none">
                                        <input bind:value={editData.displayName} class="bg-white border border-indigo-100 rounded-xl px-4 py-2 text-sm font-bold focus:ring-2 focus:ring-indigo-500 outline-none" />
                                        <input type="number" step="0.1" bind:value={editData.weightValue} class="w-20 bg-white border border-indigo-100 rounded-xl px-4 py-2 text-sm font-bold" />
                                        <button on:click={handleSaveFilter} class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-black">ОК</button>
                                    </div>
                                {:else}
                                    <h2 class="text-lg font-black text-gray-900 {filter.isActive !== false ? '' : 'text-gray-400 line-through'}">{filter.displayName}</h2>
                                    <p class="text-xs text-gray-400 mt-1">{filter.name} <span class="ml-2 px-1.5 py-0.5 bg-indigo-50 text-indigo-400 rounded text-[10px] uppercase font-black">Пользовательский</span></p>
                                {/if}
                            </div>
                        </div>
                            <div class="flex items-center gap-6">
                                <div class="flex items-center gap-2">
                                    <span class="text-sm font-black text-gray-500">Вес:</span>
                                    <span class="text-2xl font-black text-indigo-600">{filter.weight}</span>
                                    <button on:click|stopPropagation={() => startEditFilter(filter)}
                                            class="p-2 text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                        </svg>
                                    </button>
                                    <button on:click|stopPropagation={() => handleDeleteFilter(filter.id)}
                                            class="p-2 text-red-400 hover:bg-red-50 rounded-xl transition-all">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                        </svg>
                                    </button>
                                </div>
                                <div class="text-indigo-400 transition-transform {expandedParams.has('custom-' + filter.id) ? 'rotate-180' : ''}">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                                    </svg>
                                </div>
                            </div>
                        </button>

                        <!-- Custom Values -->
                        {#if expandedParams.has('custom-' + filter.id)}
                            <div class="border-t border-gray-100 p-6 space-y-3 bg-gray-50/50">
                                {#if filter.values}
                                    {#each filter.values as val (val.id)}
                                    <div class="flex items-center justify-between gap-6 p-4 bg-white rounded-2xl border border-gray-100 group">
                                        <div class="flex-1">
                                            {#if editingValueId === val.id}
                                                <input bind:value={editValueData.displayName} class="text-sm font-bold text-gray-900 border-b border-indigo-200 outline-none" />
                                            {:else}
                                                <h3 class="text-sm font-bold text-gray-900">{val.displayName}</h3>
                                                <p class="text-[10px] text-gray-400 uppercase tracking-tighter mt-0.5">Ключ: {val.valueName}</p>
                                            {/if}
                                        </div>
                                        
                                        {#if editingValueId === val.id}
                                            <div class="flex items-end gap-2">
                                                <input type="number" step="0.1" bind:value={editValueData.weightValue} 
                                                       class="w-24 bg-white border border-indigo-100 rounded-xl px-3 py-2 text-sm font-bold" />
                                                <button on:click={() => editingValueId = null} class="p-2 text-gray-400">✕</button>
                                                <button on:click={handleSaveFilterValue} class="bg-indigo-600 text-white px-4 py-2 rounded-xl text-sm font-black">ОК</button>
                                            </div>
                                        {:else}
                                            <div class="flex items-center gap-4">
                                                <span class="text-lg font-black text-indigo-600">{val.weightValue}</span>
                                                <button on:click={() => startEditFilterValue(val)} class="p-2 text-indigo-400 hover:text-indigo-600 opacity-0 group-hover:opacity-100 transition-all">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                                    </svg>
                                                </button>
                                                <button on:click={() => handleDeleteFilterValue(val.id)} class="p-2 text-red-300 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-all">
                                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                                    </svg>
                                                </button>
                                            </div>
                                        {/if}
                                    </div>
                                    {/each}
                                {/if}

                                <!-- Add New Value to Custom Filter -->
                                <div class="p-4 bg-white/50 rounded-2xl border-2 border-dashed border-gray-200 flex flex-wrap gap-4 items-end">
                                    <div class="flex-1 min-w-[150px] space-y-1">
                                        <label for="new-val-name-{filter.id}" class="text-[10px] font-black text-gray-400 uppercase tracking-widest pl-1">Тех. имя</label>
                                        <input id="new-val-name-{filter.id}" 
                                               bind:value={newValue.valueName} 
                                               on:input={(e) => newValue.valueName = e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, '')}
                                               placeholder="VALUE_1" class="w-full bg-white border border-gray-100 rounded-xl px-3 py-2 text-sm font-bold outline-none focus:border-indigo-400 uppercase" />
                                    </div>
                                    <div class="flex-[2] min-w-[200px] space-y-1">
                                        <label for="new-val-display-{filter.id}" class="text-[10px] font-black text-gray-400 uppercase tracking-widest pl-1">Отображаемое имя</label>
                                        <input id="new-val-display-{filter.id}" bind:value={newValue.displayName} placeholder="Офис 1" class="w-full bg-white border border-gray-100 rounded-xl px-3 py-2 text-sm font-bold outline-none focus:border-indigo-400" />
                                    </div>
                                    <div class="w-24 space-y-1">
                                        <label for="new-val-weight-{filter.id}" class="text-[10px] font-black text-gray-400 uppercase tracking-widest pl-1">Вес</label>
                                        <input id="new-val-weight-{filter.id}" type="number" step="0.1" bind:value={newValue.weightValue} class="w-full bg-white border border-gray-100 rounded-xl px-3 py-2 text-sm font-bold outline-none focus:border-indigo-400" />
                                    </div>
                                    <button on:click={() => handleAddFilterValue(filter.id)} disabled={$addFilterValueMutation.isPending}
                                            class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-xl text-sm font-black transition-colors disabled:opacity-50">
                                        Добавить
                                    </button>
                                </div>
                            </div>
                        {/if}
                    </div>
                {/each}
            {/key}
            {/if}

            <!-- Add New Custom Parameter Form (At the end) -->
            <div class="bg-indigo-600 p-8 rounded-3xl border border-indigo-500 shadow-xl shadow-indigo-100 space-y-6">
                <div class="flex items-center gap-4">
                    <div class="p-3 bg-white/20 rounded-2xl">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                        </svg>
                    </div>
                    <div>
                        <h2 class="text-xl font-black text-white">Новый параметр фильтрации</h2>
                        <p class="text-indigo-100 text-xs font-medium">Создайте свой критерий для оценки заявок</p>
                    </div>
                </div>
                
                <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-end">
                    <div class="space-y-2">
                        <label for="new-filter-name" class="text-[10px] font-black text-indigo-100 uppercase tracking-widest pl-1">Тех. имя (лат)</label>
                        <input id="new-filter-name" 
                               bind:value={newFilter.name} 
                               on:input={(e) => newFilter.name = e.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, '')}
                               placeholder="LOCATION" class="w-full bg-white/10 border border-white/20 rounded-xl px-4 py-3 text-sm font-bold text-white placeholder-indigo-300 outline-none focus:bg-white/20" />
                    </div>
                    <div class="space-y-2">
                        <label for="new-filter-display" class="text-[10px] font-black text-indigo-100 uppercase tracking-widest pl-1">Имя в интерфейсе</label>
                        <input id="new-filter-display" bind:value={newFilter.displayName} placeholder="Местоположение" class="w-full bg-white/10 border border-white/20 rounded-xl px-4 py-3 text-sm font-bold text-white placeholder-indigo-300 outline-none focus:bg-white/20" />
                    </div>
                    <div class="flex gap-4 items-end">
                        <div class="w-24 space-y-2">
                            <label for="new-filter-weight" class="text-[10px] font-black text-indigo-100 uppercase tracking-widest pl-1">Вес</label>
                            <input id="new-filter-weight" type="number" step="0.1" bind:value={newFilter.weight} class="w-full bg-white/10 border border-white/20 rounded-xl px-4 py-3 text-sm font-bold text-white outline-none focus:bg-white/20" />
                        </div>
                        <button on:click={handleCreateFilter} disabled={$addFilterMutation.isPending || !newFilter.name || !newFilter.displayName}
                                class="flex-1 bg-white hover:bg-indigo-50 text-indigo-600 font-black py-3 rounded-xl transition-all shadow-lg active:scale-95 disabled:opacity-50">
                            {$addFilterMutation.isPending ? 'Создание...' : 'Создать параметр'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    {/if}
</div>

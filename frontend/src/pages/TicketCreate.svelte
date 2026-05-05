<script>
    import { createMutation, createQuery } from '@tanstack/svelte-query';
    import { navigate } from "svelte-routing";
    import { api } from '../lib/api';

    const filtersQuery = createQuery({
        queryKey: ['dynamic-filters'],
        queryFn: () => api.dynamicFilters.list(),
    });

    const weightsQuery = createQuery({
        queryKey: ['priority-weights'],
        queryFn: () => api.priority.list()
    });

    let formData = {
        title: '',
        description: '',
        importance: 'MEDIUM',
        urgency: 'MEDIUM',
        impact: 'USER',
        category: 'OTHER',
        dynamicValues: {} // filterId -> valueId
    };

    let pendingFiles = [];
    let isDragging = false;
    let fileInput;

    const createMutationObj = createMutation({
        mutationFn: async (data) => {
            // Step 1: create the ticket
            const ticket = await api.tickets.create(data);
            // Step 2: upload any queued files
            for (const file of pendingFiles) {
                await api.tickets.uploadAttachment(ticket.id, file);
            }
            return ticket;
        },
        onSuccess: (ticket) => {
            navigate(`/tickets/${ticket.id}`);
        }
    });

    const labels = {
        importance: { LOW: 'Низкая', MEDIUM: 'Средняя', HIGH: 'Высокая', CRITICAL: 'Критическая' },
        urgency:    { LOW: 'Низкая', MEDIUM: 'Средняя', HIGH: 'Высокая', CRITICAL: 'Критическая' },
        impact:     { USER: 'Пользователь', DEPARTMENT: 'Отдел', ORGANIZATION: 'Организация' },
        category:   { HARDWARE: 'Оборудование', SOFTWARE: 'ПО', NETWORK: 'Сеть', ACCESS: 'Доступ', OTHER: 'Прочее' },
    };

    function handleSubmit() {
        $createMutationObj.mutate(formData);
    }

    function handleFileSelect(event) {
        const files = Array.from(event.target.files);
        addFiles(files);
        event.target.value = '';
    }

    function handleDrop(event) {
        isDragging = false;
        const files = Array.from(event.dataTransfer.files);
        addFiles(files);
    }

    function addFiles(files) {
        const newFiles = files.filter(f => !pendingFiles.some(p => p.name === f.name && p.size === f.size));
        pendingFiles = [...pendingFiles, ...newFiles];
    }

    function removeFile(index) {
        pendingFiles = pendingFiles.filter((_, i) => i !== index);
    }

    function formatFileSize(bytes) {
        if (bytes < 1024) return bytes + ' B';
        if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
        return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    }

    function isImage(file) {
        return file.type.startsWith('image/');
    }

    let previewUrls = {};
    $: {
        // Generate preview URLs for images
        for (const f of pendingFiles) {
            if (isImage(f) && !previewUrls[f.name + f.size]) {
                previewUrls[f.name + f.size] = URL.createObjectURL(f);
            }
        }
    }
</script>

<div class="max-w-3xl mx-auto space-y-8">
    <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
        <h1 class="text-3xl font-black text-gray-800 mb-8">Создание новой заявки</h1>

        <form on:submit|preventDefault={handleSubmit} class="space-y-6">
            <!-- Title -->
            <div class="space-y-1">
                <label for="title" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Тема обращения</label>
                <input
                    id="title"
                    bind:value={formData.title}
                    type="text"
                    required
                    placeholder="Краткое описание проблемы"
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-medium"
                />
            </div>

            <!-- Description -->
            <div class="space-y-1">
                <label for="description" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Подробное описание</label>
                <textarea
                    id="description"
                    bind:value={formData.description}
                    required
                    placeholder="Опишите проблему как можно подробнее..."
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all h-40 font-medium resize-none"
                ></textarea>
            </div>

            <!-- Parameters Grid -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                {#each [
                    { key: 'category',   label: 'Категория', param: 'CATEGORY' },
                    { key: 'importance', label: 'Важность',  param: 'IMPORTANCE' },
                    { key: 'urgency',    label: 'Срочность',  param: 'URGENCY' },
                    { key: 'impact',     label: 'Влияние',    param: 'IMPACT' },
                ].filter(f => {
                    if (!$weightsQuery.data) return true; // Show all while loading
                    const w = $weightsQuery.data.find(pw => pw.paramName === f.param);
                    return w ? w.active !== false : true;
                }) as field}
                    <div class="space-y-1">
                        <label for={field.key} class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">{field.label}</label>
                        <select id={field.key} bind:value={formData[field.key]}
                                class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                            {#each Object.entries(labels[field.key]) as [val, lbl]}
                                <option value={val}>{lbl}</option>
                            {/each}
                        </select>
                    </div>
                {/each}

                <!-- Dynamic Filters -->
                {#if $filtersQuery.data}
                    {#each $filtersQuery.data as filter (filter.id)}
                        <div class="space-y-1">
                            <label for="filter-{filter.id}" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">{filter.displayName}</label>
                            <select id="filter-{filter.id}" bind:value={formData.dynamicValues[filter.id]}
                                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                                <option value={undefined}>Не выбрано</option>
                                {#each filter.values as val (val.id)}
                                    <option value={val.id}>{val.displayName}</option>
                                {/each}
                            </select>
                        </div>
                    {/each}
                {/if}
            </div>

            <!-- File Attachments -->
            <div class="space-y-3">
                <span class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Вложения</span>

                <!-- Drop Zone -->
                <div
                    role="button"
                    tabindex="0"
                    class="border-2 border-dashed rounded-2xl p-6 text-center transition-all cursor-pointer {isDragging ? 'border-indigo-400 bg-indigo-50' : 'border-gray-200 hover:border-indigo-300 hover:bg-gray-50'}"
                    on:click={() => fileInput.click()}
                    on:keydown={(e) => e.key === 'Enter' && fileInput.click()}
                    on:dragover|preventDefault={() => isDragging = true}
                    on:dragleave={() => isDragging = false}
                    on:drop|preventDefault={handleDrop}
                >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 mx-auto text-gray-300 mb-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                    </svg>
                    <p class="text-sm font-bold text-gray-500">Перетащите файлы сюда или <span class="text-indigo-600">выберите</span></p>
                    <p class="text-xs text-gray-400 mt-1">Изображения, документы, архивы — до 10 МБ каждый</p>
                    <input bind:this={fileInput} type="file" class="hidden" multiple on:change={handleFileSelect} />
                </div>

                <!-- File Preview List -->
                {#if pendingFiles.length > 0}
                    <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
                        {#each pendingFiles as file, i}
                            <div class="relative group bg-gray-50 rounded-xl border border-gray-100 overflow-hidden">
                                {#if isImage(file)}
                                    <img src={previewUrls[file.name + file.size]} alt={file.name}
                                         class="w-full h-28 object-cover" />
                                {:else}
                                    <div class="h-28 flex flex-col items-center justify-center gap-2">
                                        <svg xmlns="http://www.w3.org/2000/svg" class="h-10 w-10 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                        </svg>
                                    </div>
                                {/if}
                                <div class="p-2">
                                    <p class="text-xs font-bold text-gray-700 truncate">{file.name}</p>
                                    <p class="text-[10px] text-gray-400">{formatFileSize(file.size)}</p>
                                </div>
                                <!-- Remove button -->
                                <button type="button"
                                        on:click|stopPropagation={() => removeFile(i)}
                                        class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-red-500 text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M6 18L18 6M6 6l12 12" />
                                    </svg>
                                </button>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>

            <!-- Actions -->
            <div class="pt-4 flex gap-4">
                <button type="button" on:click={() => navigate('/tickets')}
                        class="px-8 py-3 text-gray-500 font-bold hover:text-gray-700 transition-colors">
                    Отмена
                </button>
                <button type="submit" disabled={$createMutationObj.isPending}
                        class="grow bg-indigo-600 hover:bg-indigo-700 text-white font-black py-3 rounded-xl transition-colors disabled:opacity-50">
                    {#if $createMutationObj.isPending}
                        <span class="flex items-center justify-center gap-2">
                            <svg class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                            </svg>
                            {pendingFiles.length > 0 ? `Загрузка файлов...` : 'Создание...'}
                        </span>
                    {:else}
                        Создать заявку {pendingFiles.length > 0 ? `+ ${pendingFiles.length} файл(а)` : ''}
                    {/if}
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    import { createMutation } from '@tanstack/svelte-query';
    import { navigate } from "svelte-routing";
    import { api } from '../lib/api';

    const createMutationObj = createMutation({
        mutationFn: (data) => api.tickets.create(data),
        onSuccess: (ticket) => {
            navigate(`/tickets/${ticket.id}`);
        }
    });

    let formData = {
        title: '',
        description: '',
        importance: 'MEDIUM',
        urgency: 'MEDIUM',
        impact: 'USER',
        category: 'OTHER'
    };

    const importances = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const urgencies = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
    const impacts = ['USER', 'DEPARTMENT', 'ORGANIZATION'];
    const categories = ['HARDWARE', 'SOFTWARE', 'NETWORK', 'ACCESS', 'OTHER'];

    function handleSubmit() {
        $createMutationObj.mutate(formData);
    }
</script>

<div class="max-w-3xl mx-auto space-y-8">
    <div class="bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
        <h1 class="text-3xl font-black text-gray-800 mb-8">Создание новой заявки</h1>
        
        <form on:submit|preventDefault={handleSubmit} class="space-y-6">
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

            <div class="space-y-1">
                <label for="description" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Подробное описание</label>
                <textarea 
                    id="description"
                    bind:value={formData.description} 
                    required
                    placeholder="Опишите проблему как можно подробнее..."
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all h-40 font-medium"
                ></textarea>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="space-y-1">
                    <label for="category" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Категория</label>
                    <select id="category" bind:value={formData.category} class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                        {#each categories as category}
                            <option value={category}>{category}</option>
                        {/each}
                    </select>
                </div>
                
                <div class="space-y-1">
                    <label for="importance" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Важность</label>
                    <select id="importance" bind:value={formData.importance} class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                        {#each importances as importance}
                            <option value={importance}>{importance}</option>
                        {/each}
                    </select>
                </div>

                <div class="space-y-1">
                    <label for="urgency" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Срочность</label>
                    <select id="urgency" bind:value={formData.urgency} class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                        {#each urgencies as urgency}
                            <option value={urgency}>{urgency}</option>
                        {/each}
                    </select>
                </div>

                <div class="space-y-1">
                    <label for="impact" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Влияние</label>
                    <select id="impact" bind:value={formData.impact} class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                        {#each impacts as impact}
                            <option value={impact}>{impact}</option>
                        {/each}
                    </select>
                </div>
            </div>

            <div class="pt-6 flex gap-4">
                <button 
                    type="button" 
                    on:click={() => navigate('/tickets')}
                    class="px-8 py-3 text-gray-500 font-bold hover:text-gray-700 transition-colors"
                >
                    Отмена
                </button>
                <button 
                    type="submit" 
                    disabled={$createMutationObj.isPending}
                    class="grow bg-indigo-600 hover:bg-indigo-700 text-white font-black py-3 rounded-xl shadow-lg shadow-indigo-100 transform transition-all hover:-translate-y-0.5 disabled:opacity-50"
                >
                    {$createMutationObj.isPending ? 'Создание...' : 'Создать заявку'}
                </button>
            </div>
        </form>
    </div>
</div>

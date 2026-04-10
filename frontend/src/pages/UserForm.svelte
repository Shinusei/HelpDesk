<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { navigate } from "svelte-routing";
    import { api } from '../lib/api';

    export let params = {};
    const userId = params.id;
    const isEdit = !!userId;
    const queryClient = useQueryClient();

    let formData = {
        username: '',
        fullName: '',
        password: '',
        roleId: null
    };

    const userQuery = isEdit ? createQuery({
        queryKey: ['admin-user', userId],
        queryFn: () => api.admin.users.get(userId)
    }) : null;
    
    const rolesQuery = createQuery({
        queryKey: ['admin-roles'],
        queryFn: () => api.admin.users.roles()
    });

    const mutation = createMutation({
        mutationFn: (data) => isEdit ? api.admin.users.update(userId, data) : api.admin.users.create(data),
        onSuccess: () => {
            queryClient.invalidateQueries(['admin-users']);
            navigate('/admin/users');
        }
    });

    // Populate form when data loads
    // Use a reactive statement for UI updates, but avoid reactive query declarations
    $: if (isEdit && $userQuery?.data && $rolesQuery.data) {
        const user = $userQuery.data;
        const role = $rolesQuery.data.find(r => r.name === user.role);
        formData = {
            username: user.username,
            fullName: user.fullName,
            password: '',
            roleId: role ? role.id : null
        };
    }

    function handleSubmit() {
        $mutation.mutate(formData);
    }
</script>

<div class="max-w-2xl mx-auto space-y-8">
    <div class="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
        <h1 class="text-3xl font-black text-gray-800 mb-8">{isEdit ? 'Редактирование' : 'Создание'} пользователя</h1>
        
        <form on:submit|preventDefault={handleSubmit} class="space-y-6">
            <div class="space-y-1">
                <label for="username" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Логин</label>
                <input 
                    id="username"
                    bind:value={formData.username} 
                    type="text" 
                    required
                    disabled={isEdit}
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold disabled:opacity-50"
                />
            </div>

            <div class="space-y-1">
                <label for="fullName" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Полное имя</label>
                <input 
                    id="fullName"
                    bind:value={formData.fullName} 
                    type="text" 
                    required
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-medium"
                />
            </div>

            <div class="space-y-1">
                <label for="password" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">
                    Пароль {isEdit ? '(оставьте пустым, чтобы не менять)' : ''}
                </label>
                <input 
                    id="password"
                    bind:value={formData.password} 
                    type="password" 
                    required={!isEdit}
                    class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-medium"
                />
            </div>

            <div class="space-y-1">
                <label for="role" class="text-xs font-black text-gray-400 uppercase tracking-widest pl-1">Роль</label>
                <select id="role" bind:value={formData.roleId} required class="w-full bg-gray-50 border border-gray-100 rounded-xl px-4 py-3 text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-all font-bold">
                    <option value={null}>Выберите роль...</option>
                    {#if $rolesQuery.data}
                        {#each $rolesQuery.data as role}
                            <option value={role.id}>{role.name}</option>
                        {/each}
                    {/if}
                </select>
            </div>

            <div class="pt-6 flex gap-4">
                <button 
                    type="button" 
                    on:click={() => navigate('/admin/users')}
                    class="px-8 py-3 text-gray-500 font-bold hover:text-gray-700 transition-colors"
                >
                    Отмена
                </button>
                <button 
                    type="submit" 
                    disabled={$mutation.isPending}
                    class="grow bg-indigo-600 hover:bg-indigo-700 text-white font-black py-3 rounded-xl shadow-lg shadow-indigo-100 transform transition-all hover:-translate-y-0.5 disabled:opacity-50"
                >
                    {$mutation.isPending ? 'Сохранение...' : 'Сохранить'}
                </button>
            </div>
        </form>
    </div>
</div>

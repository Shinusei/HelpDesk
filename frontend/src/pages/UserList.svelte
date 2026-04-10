<script>
    import { createQuery, createMutation, useQueryClient } from '@tanstack/svelte-query';
    import { Link } from "svelte-routing";
    import { api } from '../lib/api';

    const queryClient = useQueryClient();

    const usersQuery = createQuery({
        queryKey: ['admin-users'],
        queryFn: () => api.admin.users.list(),
        refetchInterval: 10000, // Refresh every 10 seconds
    });

    const deleteMutation = createMutation({
        mutationFn: (id) => api.admin.users.delete(id),
        onSuccess: () => queryClient.invalidateQueries(['admin-users'])
    });

    function handleDelete(id, name) {
        if (confirm(`Вы уверены, что хотите удалить пользователя ${name}?`)) {
            $deleteMutation.mutate(id);
        }
    }
</script>

<div class="space-y-8">
    <div class="bg-white p-8 rounded-3xl border border-gray-100 flex justify-between items-center">
        <div>
            <h1 class="text-3xl font-black text-gray-900">Пользователи</h1>
            <p class="text-gray-500 font-medium">Управление учетными записями</p>
        </div>
        <Link to="/admin/users/new" class="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-2xl font-black transition-colors">
            Создать пользователя
        </Link>
    </div>

    {#if $usersQuery.isLoading}
        <div class="flex flex-col items-center justify-center py-20 bg-white rounded-3xl">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mb-4"></div>
            <p class="text-gray-500 font-medium">Загрузка...</p>
        </div>
    {:else if $usersQuery.data}
        <div class="bg-white rounded-3xl border border-gray-100 overflow-hidden">
            <table class="w-full text-left">
                <thead class="bg-gray-50 border-b border-gray-100">
                    <tr>
                        <th class="px-8 py-4 text-xs font-black text-gray-400 uppercase tracking-widest">ID</th>
                        <th class="px-8 py-4 text-xs font-black text-gray-400 uppercase tracking-widest">Логин</th>
                        <th class="px-8 py-4 text-xs font-black text-gray-400 uppercase tracking-widest">Полное имя</th>
                        <th class="px-8 py-4 text-xs font-black text-gray-400 uppercase tracking-widest">Роль</th>
                        <th class="px-8 py-4 text-xs font-black text-gray-400 uppercase tracking-widest text-right">Действия</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-50">
                    {#each $usersQuery.data as user}
                        <tr class="hover:bg-gray-50/50 transition-colors">
                            <td class="px-8 py-5 text-sm font-bold text-gray-400">#{user.id}</td>
                            <td class="px-8 py-5 text-sm font-black text-gray-900">{user.username}</td>
                            <td class="px-8 py-5 text-sm font-medium text-gray-600">{user.fullName}</td>
                            <td class="px-8 py-5">
                                <span class="px-3 py-1 bg-indigo-50 text-indigo-600 rounded-lg text-xs font-bold uppercase tracking-wider">
                                    {user.role}
                                </span>
                            </td>
                            <td class="px-8 py-5 text-right space-x-2">
                                <Link to="/admin/users/edit/{user.id}" class="inline-flex p-2 text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                    </svg>
                                </Link>
                                <button on:click={() => handleDelete(user.id, user.username)} class="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                                    <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                    </svg>
                                </button>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {/if}
</div>

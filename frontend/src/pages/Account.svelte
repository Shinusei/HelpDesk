<script>
  import { createMutation } from '@tanstack/svelte-query';
  import { api } from '../lib/api';

  export let user = null;

  let currentPassword = '';
  let newPassword = '';
  let confirmPassword = '';
  let success = '';
  let error = '';

  const changePasswordMutation = createMutation({
    mutationFn: async () => {
      success = '';
      error = '';
      if (!newPassword || newPassword.length < 6) throw new Error('Новый пароль должен быть не короче 6 символов');
      if (newPassword !== confirmPassword) throw new Error('Пароли не совпадают');
      await api.user.changePassword(currentPassword, newPassword);
    },
    onSuccess: () => {
      currentPassword = '';
      newPassword = '';
      confirmPassword = '';
      success = 'Пароль обновлён';
    },
    onError: (e) => {
      error = e?.message || 'Ошибка';
    }
  });
</script>

<div class="w-full flex justify-center">
  <div class="space-y-6 w-full max-w-xl">
    <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 space-y-2">
      <h1 class="text-3xl font-black text-gray-900">Управление учёткой</h1>
      <p class="text-gray-500 font-medium">
        {user?.fullName} <span class="text-gray-300">({user?.username})</span>
      </p>
    </div>

    <div class="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 space-y-4">
      <h2 class="text-lg font-black text-gray-900">Смена пароля</h2>

    {#if success}
      <div class="bg-green-50 border border-green-200 text-green-800 px-4 py-3 rounded-2xl text-sm font-bold">
        {success}
      </div>
    {/if}
    {#if error}
      <div class="bg-red-50 border border-red-200 text-red-800 px-4 py-3 rounded-2xl text-sm font-bold">
        {error}
      </div>
    {/if}

    <div class="space-y-3">
      <div>
        <label for="current-password" class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Текущий пароль</label>
        <input id="current-password" type="password" bind:value={currentPassword}
               class="w-full bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm font-bold text-gray-800 focus:ring-2 focus:ring-indigo-500 outline-none" />
      </div>
      <div>
        <label for="new-password" class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Новый пароль</label>
        <input id="new-password" type="password" bind:value={newPassword}
               class="w-full bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm font-bold text-gray-800 focus:ring-2 focus:ring-indigo-500 outline-none" />
      </div>
      <div>
        <label for="confirm-password" class="block text-[10px] font-black text-gray-400 uppercase tracking-widest mb-1">Повторите новый пароль</label>
        <input id="confirm-password" type="password" bind:value={confirmPassword}
               class="w-full bg-white border border-gray-200 rounded-xl px-4 py-2.5 text-sm font-bold text-gray-800 focus:ring-2 focus:ring-indigo-500 outline-none" />
      </div>
    </div>

      <div class="flex justify-end">
        <button
          on:click={() => $changePasswordMutation.mutate()}
          disabled={!currentPassword || !newPassword || !confirmPassword || $changePasswordMutation.isPending}
          class="bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white px-6 py-2.5 rounded-xl font-black text-sm transition-colors"
        >
          {$changePasswordMutation.isPending ? 'Сохранение...' : 'Сменить пароль'}
        </button>
      </div>
    </div>
  </div>
</div>


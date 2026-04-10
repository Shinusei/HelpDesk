<script>
  import { onMount } from 'svelte';
  import { QueryClient, QueryClientProvider } from '@tanstack/svelte-query';
  import { Router, Link, Route } from "svelte-routing";
  import { api } from './lib/api';
  
  // Pages
  import TicketList from './pages/TicketList.svelte';
  import TicketDetail from './pages/TicketDetail.svelte';
  import TicketCreate from './pages/TicketCreate.svelte';
  import Login from './pages/Login.svelte';
  import Dashboard from './pages/Dashboard.svelte';
  import UserList from './pages/UserList.svelte';
  import UserForm from './pages/UserForm.svelte';
  import PrioritySettings from './pages/PrioritySettings.svelte';

  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: 1,
        refetchOnWindowFocus: false,
      },
    },
  });

  let user = null;
  let loading = true;

  onMount(async () => {
    try {
      user = await api.user.me();
    } catch (e) {
      console.error("Auth check failed", e);
    } finally {
      loading = false;
    }
  });

  export let url = "";
</script>

<QueryClientProvider client={queryClient}>
  {#if loading}
    <div class="flex items-center justify-center h-screen bg-gray-50">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
    </div>
  {:else if !user}
    <Login />
  {:else}
    <Router {url}>
      <nav class="bg-indigo-700 text-white p-4 shadow-lg sticky top-0 z-50">
        <div class="max-w-7xl mx-auto flex justify-between items-center">
          <div class="flex items-center gap-8">
            <Link to="/" class="text-2xl font-bold tracking-tight hover:text-indigo-200 transition-colors flex items-center gap-2 mr-4">
               <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                 <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
               </svg>
               HelpDesk v2
            </Link>
            <div class="flex items-center gap-5 text-sm font-bold uppercase tracking-widest">
              <Link to="/tickets" class="hover:text-indigo-200 transition-colors">Заявки</Link>
              
              {#if user.role === 'ROLE_ADMIN' || user.role === 'ROLE_IT_SUPPORT'}
                <div class="h-4 w-px bg-indigo-500 mx-2"></div>
                <Link to="/admin" class="hover:text-indigo-200 transition-colors border-b-2 border-transparent hover:border-indigo-400">Аналитика</Link>
                {#if user.role === 'ROLE_ADMIN'}
                  <Link to="/admin/users" class="hover:text-indigo-200 transition-colors border-b-2 border-transparent hover:border-indigo-400">Пользователи</Link>
                  <Link to="/admin/priority" class="hover:text-indigo-200 transition-colors border-b-2 border-transparent hover:border-indigo-400">Настройки</Link>
                {/if}
              {/if}
            </div>
          </div>

          <div class="flex items-center gap-6">
            <Link to="/tickets/new" class="bg-indigo-500 hover:bg-indigo-600 px-5 py-2.5 rounded-xl text-sm font-black transition-all shadow-md transform hover:-translate-y-0.5">Создать</Link>
            <div class="flex items-center gap-3 border-l border-indigo-500 pl-6">
              <div class="flex flex-col items-end">
                <span class="text-xs font-black uppercase opacity-70 tracking-tighter">{user.role}</span>
                <span class="text-sm font-bold leading-none">{user.fullName}</span>
              </div>
              <a href="/logout" class="text-xs bg-red-500/80 hover:bg-red-600 px-2 py-1 rounded-lg transition-colors uppercase font-black tracking-tighter">Выйти</a>
            </div>
          </div>
        </div>
      </nav>

      <main class="max-w-7xl mx-auto p-4 md:p-8">
        <!-- Ticket Routes -->
        <Route path="/" let:params><TicketList {user} /></Route>
        <Route path="/tickets" let:params><TicketList {user} /></Route>
        <Route path="/tickets/new" component={TicketCreate} />
        <Route path="/tickets/:id" let:params>
           <TicketDetail {params} {user} />
        </Route>

        <!-- Admin Routes -->
        <Route path="/admin" let:params><Dashboard {user} /></Route>
        <Route path="/admin/users" component={UserList} />
        <Route path="/admin/users/new" component={UserForm} />
        <Route path="/admin/users/edit/:id" let:params>
           <UserForm {params} />
        </Route>
        <Route path="/admin/priority" component={PrioritySettings} />
      </main>
    </Router>
  {/if}
</QueryClientProvider>

<style>
  :global(body) {
    background-color: rgb(249 250 251); /* bg-gray-50 */
    color: rgb(17 24 39); /* text-gray-900 */
  }

  :global(.active) {
    color: white !important;
    border-bottom-width: 2px !important;
    border-color: white !important;
  }
</style>

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
  import MyTakenTickets from './pages/MyTakenTickets.svelte';
  import Account from './pages/Account.svelte';
  import UserList from './pages/UserList.svelte';
  import UserForm from './pages/UserForm.svelte';
  import PrioritySettings from './pages/PrioritySettings.svelte';
  import SupportTakenTickets from './pages/SupportTakenTickets.svelte';

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
  let menuOpen = false;

  onMount(async () => {
    try {
      user = await api.user.me();
    } catch (e) {
      console.error("Auth check failed", e);
    } finally {
      loading = false;
    }
  });

  const closeMenu = () => {
    menuOpen = false;
  };

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
      <!-- Top Navigation -->
      <nav class="bg-indigo-700 text-white p-4 sticky top-0 z-50">
        <div class="max-w-7xl mx-auto flex items-center gap-4">
          <!-- Mobile Menu Button + Logo (Left) -->
          <div class="flex items-center gap-3 flex-shrink-0">
            <button 
              on:click={() => menuOpen = !menuOpen}
              class="md:hidden p-2 hover:bg-white/10 rounded-lg transition-colors flex-shrink-0"
              aria-label="Toggle menu"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            <!-- Logo -->
            <Link to="/" class="text-xl md:text-2xl font-bold tracking-tight hover:text-indigo-200 transition-colors flex items-center gap-2 flex-shrink-0">
               <svg xmlns="http://www.w3.org/2000/svg" class="h-6 md:h-8 w-6 md:w-8 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                 <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-5 0a4 4 0 11-8 0 4 4 0 018 0z" />
               </svg>
               <span class="hidden sm:inline">HelpDesk</span>
            </Link>
          </div>

          <!-- Desktop Menu (Center) -->
          <div class="hidden md:flex items-center gap-1 text-xs lg:text-sm font-bold uppercase tracking-widest flex-1">
            <Link to="/tickets" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">Заявки</Link>
            
            {#if user.role === 'ROLE_ADMIN' || user.role === 'ROLE_IT_SUPPORT'}
              {#if user.role === 'ROLE_IT_SUPPORT'}
                <div class="h-6 w-px bg-indigo-500/80 mx-1"></div>
                <Link to="/tickets/my" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">В работе</Link>
              {/if}
              {#if user.role === 'ROLE_ADMIN'}
                <div class="h-6 w-px bg-indigo-500/80 mx-1"></div>
                <Link to="/admin" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">Аналитика</Link>
                <Link to="/admin/support-tickets" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">Взятые</Link>
                <Link to="/admin/users" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">Пользователи</Link>
                <Link to="/admin/priority" class="px-3 py-2 rounded-xl hover:bg-white/10 transition-colors whitespace-nowrap">Настройки</Link>
              {/if}
            {/if}
          </div>

          <!-- Right Side (Buttons) -->
          <div class="flex items-center gap-2 md:gap-6 flex-shrink-0 ml-auto">
            <Link to="/tickets/new" class="bg-indigo-500 hover:bg-indigo-600 text-white px-3 md:px-5 py-2 md:py-2.5 rounded-xl text-xs md:text-sm font-black transition-colors whitespace-nowrap">Создать</Link>
            <div class="hidden md:flex items-center gap-3 border-l border-indigo-500 pl-6">
              <Link to="/account" class="group bg-white/10 hover:bg-white/15 transition-colors px-3 py-2 rounded-2xl">
                <div class="flex flex-col items-end">
                  <span class="text-xs font-black uppercase opacity-70 tracking-tighter leading-none">{user.role}</span>
                  <span class="text-sm font-bold leading-none group-hover:text-indigo-100 transition-colors">{user.fullName}</span>
                </div>
              </Link>
            </div>
            <a href="/logout" 
               class="p-2.5 bg-red-500/10 hover:bg-red-500 text-red-500 hover:text-white rounded-xl transition-all duration-300 group shadow-sm hover:shadow-red-200" 
               title="Выйти"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </a>
          </div>
        </div>
      </nav>

      <!-- Mobile Sidebar -->
      {#if menuOpen}
        <div 
          class="fixed inset-0 bg-black/50 md:hidden z-40" 
          on:click={closeMenu}
          role="presentation"
        ></div>
        <div class="fixed left-0 top-16 bottom-0 w-64 bg-indigo-800 text-white overflow-y-auto z-40 md:hidden shadow-lg">
          <div class="p-4 space-y-2">
            <Link 
              to="/tickets" 
              on:click={closeMenu}
              class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
            >
              Заявки
            </Link>
            
            {#if user.role === 'ROLE_ADMIN' || user.role === 'ROLE_IT_SUPPORT'}
              {#if user.role === 'ROLE_IT_SUPPORT'}
                <Link 
                  to="/tickets/my" 
                  on:click={closeMenu}
                  class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
                >
                  В работе
                </Link>
              {/if}
              <Link 
                to="/admin" 
                on:click={closeMenu}
                class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
              >
                Аналитика
              </Link>
              {#if user.role === 'ROLE_ADMIN'}
                <Link 
                  to="/admin/support-tickets" 
                  on:click={closeMenu}
                  class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
                >
                  Взятые заявки
                </Link>
                <Link 
                  to="/admin/users" 
                  on:click={closeMenu}
                  class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
                >
                  Пользователи
                </Link>
                <Link 
                  to="/admin/priority" 
                  on:click={closeMenu}
                  class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
                >
                  Настройки приоритетов
                </Link>
              {/if}
            {/if}

            <div class="my-4 border-t border-indigo-500/50"></div>

            <Link 
              to="/account" 
              on:click={closeMenu}
              class="block w-full text-left px-4 py-3 rounded-xl hover:bg-white/10 transition-colors font-semibold"
            >
              <div class="flex flex-col">
                <span class="text-xs font-black uppercase opacity-70 tracking-tighter">{user.role}</span>
                <span class="text-sm font-bold">{user.fullName}</span>
              </div>
            </Link>
          </div>
        </div>
      {/if}

      <main class="max-w-7xl mx-auto p-4 md:p-8">
        <!-- Ticket Routes -->
        <Route path="/" let:params><TicketList {user} /></Route>
        <Route path="/tickets" let:params><TicketList {user} /></Route>
        <Route path="/tickets/my" let:params><MyTakenTickets {user} /></Route>
        <Route path="/tickets/new" component={TicketCreate} />
        <Route path="/account" let:params><Account {user} /></Route>
        <Route path="/tickets/:id" let:params>
           <TicketDetail {params} {user} />
        </Route>

        <!-- Admin Routes -->
        <Route path="/admin" let:params><Dashboard {user} /></Route>
        <Route path="/admin/support-tickets" let:params><SupportTakenTickets {user} /></Route>
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

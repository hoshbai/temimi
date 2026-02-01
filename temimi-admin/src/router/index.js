import { createRouter, createWebHistory } from "vue-router";
const Index = () => import("@/views/IndexVue.vue");
const NotFound = () => import("@/views/NotFound.vue");
const Login = () => import("@/views/LoginVue.vue");
const Home = () => import("@/views/homePage/HomePage.vue");
const Carousel = () => import("@/views/content/CarouselManage.vue");
const HotSearch = () => import("@/views/content/HotSearchManage.vue");
const Ranking = () => import("@/views/content/RankingManage.vue");
const Tag = () => import("@/views/content/TagManage.vue");
const Video = () => import("@/views/review/VideoReview.vue");
const VideoDetail = () => import("@/views/review/detail/VideoDetail.vue");
const Article = () => import("@/views/review/ArticleReview.vue");
const Avatar = () => import("@/views/review/AvatarReview.vue");
const Dynamic = () => import("@/views/review/DynamicReview.vue");
const User = () => import("@/views/system/UserManage.vue");
const Role = () => import("@/views/system/RoleManage.vue");

const routes = [
  { path: "/", redirect: "" },
  {
    path: "",
    redirect: "/home",
    component: Index,
    meta: { 
      requiresAuth: true,
      layout: 'admin'
    },
    children: [
      // Dashboard
      { 
        path: '/home', 
        name: 'home',
        component: Home, 
        meta: { 
          requiresAuth: true,
          title: 'Dashboard',
          feature: 'dashboard'
        } 
      },
      
      // Content Management
      {
        path: '/content',
        redirect: '/content/carousel',
        meta: { 
          requiresAuth: true,
          feature: 'content'
        },
        children: [
          { 
            path: '/content/carousel', 
            name: 'content-carousel',
            component: Carousel, 
            meta: { 
              requiresAuth: true,
              title: 'Carousel Management'
            } 
          },
          { 
            path: '/content/hot-search', 
            name: 'content-hot-search',
            component: HotSearch, 
            meta: { 
              requiresAuth: true,
              title: 'Hot Search Management'
            } 
          },
          { 
            path: '/content/ranking', 
            name: 'content-ranking',
            component: Ranking, 
            meta: { 
              requiresAuth: true,
              title: 'Ranking Management'
            } 
          },
          { 
            path: '/content/tag', 
            name: 'content-tag',
            component: Tag, 
            meta: { 
              requiresAuth: true,
              title: 'Tag Management'
            } 
          },
        ]
      },
      
      // Review Management
      {
        path: '/review',
        redirect: '/review/video',
        meta: { 
          requiresAuth: true,
          feature: 'review'
        },
        children: [
          {
            path: '/review/video',
            redirect: '/review/video/form',
            children: [
              { 
                path: '/review/video/form', 
                name: 'review-video',
                component: Video, 
                meta: { 
                  requiresAuth: true,
                  title: 'Video Review'
                } 
              },
              { 
                path: '/review/video/detail/:vid', 
                name: 'review-video-detail', 
                component: VideoDetail, 
                meta: { 
                  requiresAuth: true,
                  title: 'Video Detail'
                } 
              },
            ]
          },
          { 
            path: '/review/article', 
            name: 'review-article',
            component: Article, 
            meta: { 
              requiresAuth: true,
              title: 'Article Review'
            } 
          },
          { 
            path: '/review/avatar', 
            name: 'review-avatar',
            component: Avatar, 
            meta: { 
              requiresAuth: true,
              title: 'Avatar Review'
            } 
          },
          { 
            path: '/review/dynamic', 
            name: 'review-dynamic',
            component: Dynamic, 
            meta: { 
              requiresAuth: true,
              title: 'Dynamic Review'
            } 
          },
        ]
      },
      
      // System Management
      {
        path: '/system',
        redirect: '/system/user',
        meta: { 
          requiresAuth: true,
          feature: 'system'
        },
        children: [
          { 
            path: '/system/user', 
            name: 'system-user',
            component: User, 
            meta: { 
              requiresAuth: true,
              title: 'User Management'
            } 
          },
          { 
            path: '/system/role', 
            name: 'system-role',
            component: Role, 
            meta: { 
              requiresAuth: true,
              title: 'Role Management'
            } 
          },
        ]
      },
    ]
  },
  
  // Login
  {
    path: "/login",
    name: "login",
    component: Login,
    meta: { 
      requiresAuth: false,
      title: 'Admin Login'
    },
  },
  
  // 404 Not Found
  {
    path: "/:catchAll(.*)",
    name: "notfound",
    component: NotFound,
    meta: { 
      requiresAuth: false,
      title: 'Page Not Found'
    },
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// Navigation guard for authentication
router.beforeEach(async (to, from, next) => {
  // Import user store dynamically to avoid circular dependency
  const { useUserStore } = await import('@/stores/user')
  const userStore = useUserStore()
  
  // Check if route requires authentication
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  
  // Get token from localStorage
  const token = localStorage.getItem('teri_token')
  
  if (requiresAuth) {
    if (!token) {
      // No token, redirect to login
      next({ 
        path: '/login', 
        query: { redirect: to.fullPath } 
      })
    } else {
      // Has token, check if user info is loaded
      if (!userStore.isLogin) {
        try {
          // Try to get user info
          await userStore.getPersonalInfo()
          next()
        } catch (error) {
          // Failed to get user info, redirect to login
          next({ 
            path: '/login', 
            query: { redirect: to.fullPath } 
          })
        }
      } else {
        // User is already logged in
        next()
      }
    }
  } else {
    // Route doesn't require auth
    // If already logged in and trying to access login page, redirect to home
    if (to.path === '/login' && token && userStore.isLogin) {
      next({ path: '/home' })
    } else {
      next()
    }
  }
})

// Update document title based on route meta
router.afterEach((to) => {
  const title = to.meta.title ? `${to.meta.title} - Temimi Admin` : 'Temimi Admin'
  document.title = title
})

export default router;

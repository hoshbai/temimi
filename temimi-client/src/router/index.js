import { createRouter, createWebHistory } from 'vue-router'
const Index = () => import('views/IndexVue.vue')
const NotFound = () => import('@/views/NotFound.vue')
const Platform = () => import('@/views/platform/PlatformView.vue')
const PlatformHome = () => import('@/views/platform/children/PlatformHome.vue')
const PlatformUpload = () => import('@/views/platform/children/PlatformUpload.vue')
const PlatformManuscript = () => import('@/views/platform/children/PlatformManuscript.vue')
const PlatformAppeal = () => import('@/views/platform/children/PlatformAppeal.vue')
const PlatformData = () => import('@/views/platform/children/PlatformData.vue')
const PlatformComment = () => import('@/views/platform/children/PlatformComment.vue')
const PlatformDanmu = () => import('@/views/platform/children/PlatformDanmu.vue')
const VideoUpload = () => import('@/views/platform/children/uploadChildren/VideoUpload.vue')
const TextUpload = () => import('@/views/platform/children/uploadChildren/TextUpload.vue')
const VideoDetail = () => import('@/views/detail/VideoDetail.vue')
const Message = () => import('@/views/message/MessageView.vue')
const MessageReply = () => import('@/views/message/children/MessageReply.vue')
const MessageAt = () => import('@/views/message/children/MessageAt.vue')
const MessageLove = () => import('@/views/message/children/MessageLove.vue')
const MessageSystem = () => import('@/views/message/children/MessageSystem.vue')
const MessageWhisper = () => import('@/views/message/children/MessageWhisper.vue')
const MessageConfig = () => import('@/views/message/children/MessageConfig.vue')
const WhisperDialog = () => import('@/views/message/children/children/WhisperDialog.vue')
const Search = () => import('@/views/search/SearchView.vue')
const SearchVideo = () => import('@/views/search/children/SearchVideo.vue')
const SearchUser = () => import('@/views/search/children/SearchUser.vue')
const Space = () => import('@/views/space/SpaceView.vue')
const SpaceHome = () => import('@/views/space/children/SpaceHome.vue')
const SpaceVideo = () => import('@/views/space/children/SpaceVideo.vue')
const SpaceArticle = () => import('@/views/space/children/SpaceArticle.vue')
const SpaceDynamic = () => import('@/views/space/children/SpaceDynamic.vue')
const SpaceFavlist = () => import('@/views/space/children/SpaceFavlist.vue')
const SpaceSetting = () => import('@/views/space/children/SpaceSetting.vue')
const SpaceFollow = () => import('@/views/space/children/SpaceFollow.vue')
const SpaceFans = () => import('@/views/space/children/SpaceFans.vue')
const SpaceHistory = () => import('@/views/space/children/SpaceHistory.vue')
const Account = () => import('@/views/account/AccountView.vue')
const AccountHome = () => import('@/views/account/children/AccountHome.vue')
const AccountInfo = () => import('@/views/account/children/AccountInfo.vue')
const AccountAvatar = () => import('@/views/account/children/AccountAvatar.vue')
const AccountSecurity = () => import('@/views/account/children/AccountSecurity.vue')


const routes = [
    // Home
    { 
        path: '/', 
        redirect: '' 
    },
    { 
        path: '', 
        name: "index", 
        component: Index, 
        meta: { 
            requiresAuth: false,
            title: 'Temimi - Home'
        } 
    },
    
    // Platform (Creator Center)
    {
        path: '/platform',
        redirect: '/platform/home',
        component: Platform,
        meta: { 
            requiresAuth: true,
            feature: 'platform'
        },
        children: [
            { 
                path: '/platform/home', 
                name: 'platform-home',
                component: PlatformHome, 
                meta: { 
                    requiresAuth: true,
                    title: 'Creator Center - Home'
                } 
            },
            {
                path: '/platform/upload',
                component: PlatformUpload,
                redirect: '/platform/upload/video',
                meta: { 
                    requiresAuth: true,
                    feature: 'upload'
                },
                children: [
                    { 
                        path: '/platform/upload/video', 
                        name: 'upload-video',
                        component: VideoUpload, 
                        meta: { 
                            requiresAuth: true,
                            title: 'Upload Video'
                        } 
                    },
                    { 
                        path: '/platform/upload/text', 
                        name: 'upload-text',
                        component: TextUpload, 
                        meta: { 
                            requiresAuth: true,
                            title: 'Upload Article'
                        } 
                    },
                ]
            },
            { 
                path: '/platform/upload-manager', 
                redirect: '/platform/upload-manager/manuscript' 
            },
            { 
                path: '/platform/upload-manager/manuscript', 
                name: 'manuscript-manager',
                component: PlatformManuscript, 
                meta: { 
                    requiresAuth: true,
                    title: 'Manuscript Manager'
                } 
            },
            { 
                path: '/platform/upload-manager/appeal', 
                name: 'appeal-manager',
                component: PlatformAppeal, 
                meta: { 
                    requiresAuth: true,
                    title: 'Appeal Manager'
                } 
            },
            { 
                path: '/platform/data-up', 
                name: 'data-center',
                component: PlatformData, 
                meta: { 
                    requiresAuth: true,
                    title: 'Data Center'
                } 
            },
            { 
                path: '/platform/comment', 
                name: 'comment-manager',
                component: PlatformComment, 
                meta: { 
                    requiresAuth: true,
                    title: 'Comment Manager'
                } 
            },
            { 
                path: '/platform/danmu', 
                name: 'danmu-manager',
                component: PlatformDanmu, 
                meta: { 
                    requiresAuth: true,
                    title: 'Danmu Manager'
                } 
            },
        ]
    },
    
    // Message Center
    {
        path: '/message',
        redirect: '/message/reply',
        component: Message,
        meta: { 
            requiresAuth: true,
            feature: 'message'
        },
        children: [
            { 
                path: '/message/reply', 
                name: 'message-reply',
                component: MessageReply, 
                meta: { 
                    requiresAuth: true,
                    title: 'Replies'
                } 
            },
            { 
                path: '/message/at', 
                name: 'message-at',
                component: MessageAt, 
                meta: { 
                    requiresAuth: true,
                    title: 'Mentions'
                } 
            },
            { 
                path: '/message/love', 
                name: 'message-love',
                component: MessageLove, 
                meta: { 
                    requiresAuth: true,
                    title: 'Likes'
                } 
            },
            { 
                path: '/message/system', 
                name: 'message-system',
                component: MessageSystem, 
                meta: { 
                    requiresAuth: true,
                    title: 'System Messages'
                } 
            },
            {
                path: '/message/whisper', 
                component: MessageWhisper, 
                meta: { 
                    requiresAuth: true,
                    title: 'Private Messages'
                },
                children: [
                    { 
                        path: '/message/whisper/:mid', 
                        name: 'whisper-dialog',
                        component: WhisperDialog, 
                        meta: { 
                            requiresAuth: true,
                            title: 'Chat'
                        } 
                    }
                ]
            },
            { 
                path: '/message/config', 
                name: 'message-config',
                component: MessageConfig, 
                meta: { 
                    requiresAuth: true,
                    title: 'Message Settings'
                } 
            },
        ]
    },
    
    // Video Detail
    { 
        path: '/video/:vid', 
        name: 'video-detail',
        component: VideoDetail, 
        meta: { 
            requiresAuth: false,
            title: 'Video',
            feature: 'video'
        } 
    },
    
    // Search
    {
        path: '/search',
        component: Search,
        meta: { 
            requiresAuth: false,
            feature: 'search'
        },
        props: route => ({ keyword: route.query.keyword }),
        children: [
            { 
                path: '/search/video', 
                name: 'search-video',
                component: SearchVideo, 
                meta: { 
                    requiresAuth: false,
                    title: 'Search Videos'
                }, 
                props: route => ({ keyword: route.query.keyword }) 
            },
            { 
                path: '/search/user', 
                name: 'search-user',
                component: SearchUser, 
                meta: { 
                    requiresAuth: false,
                    title: 'Search Users'
                }, 
                props: route => ({ keyword: route.query.keyword }) 
            },
        ]
    },
    
    // User Space
    {
        path: '/space',
        component: Space,
        meta: { 
            requiresAuth: false,
            feature: 'space'
        },
        children: [
            { 
                path: '/space/:uid', 
                name: 'space-home',
                component: SpaceHome, 
                meta: { 
                    requiresAuth: false,
                    title: 'User Space'
                } 
            },
            { 
                path: '/space/:uid/video', 
                name: 'space-video',
                component: SpaceVideo, 
                meta: { 
                    requiresAuth: false,
                    title: 'Videos'
                } 
            },
            { 
                path: '/space/:uid/article', 
                name: 'space-article',
                component: SpaceArticle, 
                meta: { 
                    requiresAuth: false,
                    title: 'Articles'
                } 
            },
            { 
                path: '/space/:uid/dynamic', 
                name: 'space-dynamic',
                component: SpaceDynamic, 
                meta: { 
                    requiresAuth: false,
                    title: 'Dynamics'
                } 
            },
            { 
                path: '/space/:uid/favlist', 
                name: 'space-favlist',
                component: SpaceFavlist, 
                meta: { 
                    requiresAuth: false,
                    title: 'Favorites'
                }, 
                props: route => ({ fid: route.query.fid }) 
            },
            { 
                path: '/space/:uid/setting', 
                name: 'space-setting',
                component: SpaceSetting, 
                meta: { 
                    requiresAuth: true,
                    title: 'Space Settings'
                } 
            },
            { 
                path: '/space/:uid/fans/follow', 
                name: 'space-follow',
                component: SpaceFollow, 
                meta: { 
                    requiresAuth: false,
                    title: 'Following'
                } 
            },
            { 
                path: '/space/:uid/fans/fans', 
                name: 'space-fans',
                component: SpaceFans, 
                meta: { 
                    requiresAuth: false,
                    title: 'Fans'
                } 
            },
            { 
                path: '/space/:uid/history', 
                name: 'space-history',
                component: SpaceHistory, 
                meta: { 
                    requiresAuth: true,
                    title: '历史记录'
                } 
            },
        ]
    },
    
    // Account Settings
    {
        path: '/account',
        redirect: '/account/home',
        component: Account,
        meta: { 
            requiresAuth: true,
            feature: 'account'
        },
        children: [
            { 
                path: '/account/home', 
                name: 'account-home',
                component: AccountHome, 
                meta: { 
                    requiresAuth: true,
                    title: 'Account Center'
                } 
            },
            { 
                path: '/account/info', 
                name: 'account-info',
                component: AccountInfo, 
                meta: { 
                    requiresAuth: true,
                    title: 'Personal Info'
                } 
            },
            { 
                path: '/account/avatar', 
                name: 'account-avatar',
                component: AccountAvatar, 
                meta: { 
                    requiresAuth: true,
                    title: 'Avatar Settings'
                } 
            },
            { 
                path: '/account/security', 
                name: 'account-security',
                component: AccountSecurity, 
                meta: { 
                    requiresAuth: true,
                    title: 'Security Settings'
                } 
            },
        ]
    },
    
    // 404 Not Found
    { 
        path: '/:catchAll(.*)', 
        name: "notfound", 
        component: NotFound, 
        meta: { 
            requiresAuth: false,
            title: 'Page Not Found'
        } 
    },

]

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes
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
            // No token, redirect to home and trigger login modal
            userStore.openLogin = true
            next({ path: '/', query: { redirect: to.fullPath } })
        } else {
            // Has token, check if user info is loaded
            if (!userStore.isLogin) {
                try {
                    // Try to get user info
                    await userStore.getPersonalInfo()
                    next()
                } catch (error) {
                    // Failed to get user info, redirect to home
                    userStore.openLogin = true
                    next({ path: '/', query: { redirect: to.fullPath } })
                }
            } else {
                // User is already logged in
                next()
            }
        }
    } else {
        // Route doesn't require auth
        next()
    }
})

// Update document title based on route meta
router.afterEach((to) => {
    const title = to.meta.title || 'Temimi'
    document.title = title
})

export default router
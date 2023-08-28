import Vue from 'vue'
import Router from 'vue-router'
import store from '../vuex'
import { hasPerm } from "../common/permission";
import { Loading } from 'element-ui'

import index from '@/view/index'
import home from '@/view/home/home'
import error from "@/view/error/error"
import forceChangePwd from "@/view/changepwd/force"

import login from '@/view/login/login'

import decision from '@/view/decision/decision'
import decisionProject from '@/view/decision/project/project'
import decisionFunc from '@/view/decision/func/func'
import decisionThirdApi from '@/view/decision/thirdapi/thirdapi'
import decisionAiModel from '@/view/decision/aimodel/aimodel'
import decisionAssets from '@/view/decision/project/assets/assets'
import decisionKnowledgePackage from '@/view/decision/project/knowledgepackage/knowledgepackage'

import settings from "@/view/settings/home.vue"
import orgSettings from '@/view/settings/org'
import userSettings from '@/view/settings/user'
import roleSettings from '@/view/settings/role'
import permissionSettings from '@/view/settings/permission'
import loginSettings from '@/view/settings/login'
import logs from '@/view/settings/logs'

import micro from "@/view/microservice/home"
import microList from "@/view/microservice/list"
import microAiModel from "@/view/microservice/aimodel"
import microApiDoc from "@/view/microservice/apidoc"
import microClient from "@/view/microservice/client/index"
import token from '@/view/microservice/token/index.vue'

import approval from '@/view/microservice/report/approval'
import detail from '@/view/microservice/report/detail'
import summary from '@/view/microservice/report/summary'
// import VueCookie from "vue-cookies"

Vue.use(Router)

const router = new Router({
    routes: [{
            path: '/',
            name: 'index',
            component: index,
            redirect: '/decision',
            children: [{
                    path: '/home',
                    name: 'home',
                    component: home
                },
                {
                    path: '/decision',
                    name: 'decision',
                    component: decision,
                    meta: { permisson: 'menu:view:decision' },
                    children: [{
                            path: '/decision/project',
                            name: 'decisionProject',
                            component: decisionProject,
                            meta: { permisson: 'project:view' },
                        },
                        {
                            path: '/decision/func',
                            name: 'decisionFunc',
                            meta: { permisson: 'function:view' },
                            component: decisionFunc
                        },
                        {
                            path: '/decision/thirdapi',
                            name: 'decisionThirdApi',
                            meta: { permisson: 'thirdapi:view' },
                            component: decisionThirdApi
                        },
                        {
                            path: '/decision/aimodel',
                            name: 'decisionAiModel',
                            meta: { permisson: 'aimodel:view' },
                            component: decisionAiModel
                        }
                    ]
                },
                {
                    path: '/decision/project/:uuid',
                    name: 'decisionProjectHome',
                    component: decisionAssets
                },
                {
                    path: '/decision/project/:uuid/assets/:assetsUuid',
                    name: 'decisionAssets',
                    component: decisionAssets
                },
                {
                    path: '/decision/project/:uuid/knowledgepackage',
                    name: 'decisionKnowledgePackage',
                    component: decisionAssets
                },
                {
                    path: '/settings',
                    name: 'settings',
                    meta: { permisson: 'menu:view:systemadmin' },
                    component: settings,
                    children: [{
                            path: '/settings/org',
                            name: 'org',
                            meta: { permisson: 'org:view' },
                            component: orgSettings
                        },
                        {
                            path: '/settings/user',
                            name: 'user',
                            meta: { permisson: 'user:view' },
                            component: userSettings
                        },
                        {
                            path: '/settings/role',
                            meta: { permisson: 'role:list' },
                            name: 'roleSettings',
                            component: roleSettings
                        },
                        {
                            path: '/settings/permission',
                            name: 'permissionSettings',
                            component: permissionSettings
                        },
                        {
                            path: '/settings/login',
                            name: 'loginSettings',
                            meta: { permisson: 'login:view' },
                            component: loginSettings
                        },
                        {
                            path: '/settings/logs',
                            name: 'logs',
                            meta: { permisson: 'log:view' },
                            component: logs
                        },

                    ]
                },
                {
                    path: '/microservice',
                    name: 'micro',
                    meta: { permisson: 'menu:view:microservice' },
                    component: micro,
                    children: [{
                            path: '/microservice/list',
                            name: 'microList',
                            meta: { permisson: 'microservice:view' },
                            component: microList
                        },
                        {
                            path: '/microservice/aimodel',
                            name: 'microAiModel',
                            component: microAiModel
                        },
                        {
                            path: '/microservice/:uuid/apidoc',
                            name: 'microApiDoc',
                            component: microApiDoc
                        },
                        {
                            path: '/microservice/client',
                            name: 'microClient',
                            meta: { permisson: 'client:view' },
                            component: microClient
                        },
                        {
                            path: '/microservice/token',
                            name: 'token',
                            component: token,
                            meta: { permisson: 'client:view' },
                        },
                        {
                            path: '/microservice/report/summary',
                            name: 'summary',
                            component: summary,
                            meta: { permisson: 'report:view' },
                        },
                        {
                            path: '/microservice/report/detail',
                            name: 'detail',
                            component: detail,
                            meta: { permisson: 'report:view' },
                        },
                        {
                            path: '/microservice/report/approval',
                            name: 'approval',
                            component: approval,
                            meta: { permisson: 'report:view' },
                        }
                    ]
                }
            ]
        },
        {
            path: '/forcechangepwd',
            name: 'forceChangePwd',
            component: forceChangePwd
        },
        {
            path: '/login',
            name: 'login',
            component: login
        },
        {
            path: '/nolicense',
            name: 'nolicense',
            component: error
        },
        {
            path: '/dqclogin',
            name: 'dqclogin',
            component: error
        },
        {
            path: '/500',
            name: '500',
            component: error
        },
        {
            path: '/403',
            name: '403',
            component: error
        },
        {
            path: '*',
            name: '404',
            component: error
        }
    ]
})

const whiteList = ['login', '404', '500', 'nolicense']

const permNext = (to, next) => {
    // 进入路由之前判断权限
    if (!to.meta || !to.meta.permisson) {
        next()
        return
    }

    if (hasPerm(to.meta.permisson)) {
        next()
    } else {
        if (to.name == 'decision') {
            if (hasPerm('menu:view:microservice')) {
                next('/microservice')
            } else if (hasPerm('menu:view:systemadmin')) {
                next('/settings')
            } else {
                next('/403')
            }
        } else {
            next('/403')
        }
    }
}

router.beforeEach((to, from, next) => {
    // //dqc 用户标志
    // let token = to.query.token
    // if (token) {
    //   store.state.Authorization = token
    //   var curTime = new Date();
    //   var expire = new Date(curTime.setMinutes(curTime.getMinutes() + 30));
    //   VueCookie.set('Authorization', token, expire)
    // } else{
    //   let cookie = VueCookie.get('Authorization')
    //   if(cookie) {
    //     store.state.Authorization = token
    //   }
    // }
    if (localStorage.getItem('hasLogin') === '1') {
        // 已经登录
        if (to.name == 'login') {
            // 已登录状态访问登录页面，转到首页
            next('/')
            return
        } else if (to.name == 'nolicense') {
            next()
        } else if (store.state.user == null) {
            // 没有用户信息，先调用接口获取再跳转
            const loading = Loading.service({
              text: '数据加载中...'
            });
            store.dispatch('getUserProfile').then(() => {
                loading.close()
                permNext(to, next)
            }).catch(e => {
                loading.close()
                if (to.name != '403') {
                    next('/403')
                } else {
                    next()
                }
            })
        } else {
            permNext(to, next)
        }
    } else if (whiteList.indexOf(to.name) != -1) {
        // 跳转白名单页面
        next()
    } else {
        // 没登录又不是白名单页面，跳转去登录
        next('/login')
    }
})

export default router

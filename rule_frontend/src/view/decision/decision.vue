<template>
  <el-container>
    <el-aside width="210px">
      <el-menu
        :default-active="$route.path"
        :router="true"
        background-color="#20222a"
        text-color="#fff"
        active-text-color="#009688">
        <el-menu-item
          v-for="(e, i) in menus"
          :key="i"
          :index="e.path"
          v-if="$hasPerm(e.permission)">
          <i :class="e.icon"></i>
          <span slot="title">{{e.label}}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
  export default {
    name: "decision",
    data() {
      return {
        menus: [
          {
            path: "/decision/project",
            label: "项目列表",
            icon: "iconfont iconlist-2-copy",
            permission: "project:view"
          },
          {
            path: "/decision/func",
            label: "函数库",
            icon: "iconfont iconhanshu",
            permission: "function:view"
          },
          {
            path: "/decision/aimodel",
            label: "模型配置",
            icon: "iconfont iconAI",
            permission: "aimodel:view"
          }
        ]
      }
    },
    watch: {
      '$route': function(newVal) {
        if (newVal.name == 'decision') {
          this.toFirstMenu()
        }
      }
    },
    mounted() {
      if (this.$route.name == 'decision') {
        this.toFirstMenu()
      }
    },
    methods: {
      toFirstMenu() {
        this.menus.some(e => {
          if (this.$hasPerm(e.permission)) {
            this.$router.push(e.path)
            return true
          }
        })
      }
    }
  }
</script>

<style scoped>

</style>

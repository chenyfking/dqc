<template>
  <el-container>
    <el-aside class="menu" width="210px">
      <el-menu
        :default-active="$route.path"
        :router="true"
        background-color="#20222a"
        text-color="#fff"
        active-text-color="#009688">
        <div v-for="(e, i) in menus" :key="i" v-if="$hasPerm(e.permission)">
          <el-menu-item :index="e.path">
            <i :class="e.icon"></i>
            <span slot="title">{{e.label}}</span>
          </el-menu-item>
        </div>
      </el-menu>
    </el-aside>
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>
<script>
export default {
  data() {
    return {
      menus: [
        {
          path: "/settings/org",
          label: "机构管理",
          icon: "iconfont icongongsi",
          permission: "org:view"
        },
        {
          path: "/settings/user",
          label: "用户管理",
          icon: "iconfont iconyonghuguanli",
          permission: "user:view"
        },
        {
          path: "/settings/role",
          label: "角色权限",
          icon: "iconfont iconyonghu",
          permission: "role:list"
        },
        {
          path: "/settings/logs",
          label: "操作日志",
          icon: "iconfont iconpingfenqia",
          permission: "log:view"
        },
        {
          path: "/settings/login",
          label: "登录信息",
          icon: "iconfont icondenglu",
          permission: "login:view"
        }
      ]
    };
  },
  mounted() {
    if (this.$route.name == "settings") {
      this.linkRouter();
    }
  },
  watch: {
    $route(to, from) {
      if (to.name == "settings") {
        this.linkRouter();
      }
    }
  },
  methods: {
    linkRouter() {
      let arr = [];
      this.menus.forEach((item, index) => {
        if (this.$hasPerm(item.permission)) {
          arr.push(item);
          return;
        }
      });
      if (arr.length) {
        this.$router.push(arr[0].path);
      }
    },
  }
};
</script>

<style>
.active .ant-table-fixed-left table {
  width: 800px;
}
</style>

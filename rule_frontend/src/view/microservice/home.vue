<template>
  <el-container>
    <el-aside class="menu" width="210px">
      <el-menu
        :default-active="$route.path"
        :router="true"
        background-color="#20222a"
        text-color="#fff"
        active-text-color="#009688">
        <el-menu-item
          v-for="e in menus"
          :key="e.path"
          :index="e.path"
          v-if="$hasPerm(e.permission) && !e.children">
          <i :class="e.icon" class="micon"></i>
          <span slot="title">{{e.label}}</span>
        </el-menu-item>
        <el-submenu
          v-for="e,i in menus"
          :key="e.path"
          :index="e.path"
          v-if="$hasPerm(e.permission) && e.children">
          <template slot="title">
            <i :class="e.icon" class="micon" />
            <span>{{e.label}}</span>
          </template>
          <el-menu-item v-for="c in e.children" :key="c.path" :index="c.path">{{c.label}}</el-menu-item>
        </el-submenu>
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
          path: "/microservice/list",
          label: "服务列表",
          icon: "iconfont iconlist-2-copy",
          permission: "microservice:view",
        },
        {
          path: "/microservice/token",
          label: "令牌管理",
          icon: "el-icon-medal",
          permission: "token:view",
        },
        {
          path: "/microservice/client",
          label: "集群节点",
          icon: "iconfont iconfuwuqi",
          permission: "client:view",
        },
        {
          path: "/microservice/report/summary",
          label: "统计报表",
          icon: "el-icon-s-data",
          permission: "report:view",
          children: [
            {
              path: "/microservice/report/summary",
              label: "汇总报表"
            },
            {
              path: "/microservice/report/detail",
              label: "明细报表"
            },
            {
              path: "/microservice/report/approval",
              label: "审批类报表"
            }
          ]
        }
      ],
    };
  },
  watch: {
    $route(to, from) {
      if (to.name == "micro") {
        this.linkRouter();
      }
    },
  },
  mounted() {
    if (this.$route.name == "micro") {
      this.linkRouter();
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
  },
};
</script>

<style>
.micon {
  font-size: 22px;
}
</style>

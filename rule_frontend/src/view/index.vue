<template>
  <el-container>
    <el-header class="header" v-if="false">
      <div class="headImg">
        <router-link :to="'/'" class="headWidth"></router-link>
        <div style="display: inline-block;">
          <div class="menu_list" :class="{active : menuIndex == 1 || isCurrentModule('/decision')}"
               @mouseover="menuIndex = 1" @mouseout="menuIndex = -1"
               v-if="$hasPerm('menu:view:decision')">
            <div class="dropdown">
              <router-link class="menu_manage dropdown-toggle" :to="'/decision'">
                决策建模
              </router-link>
            </div>
          </div>
          <div class="menu_list" :class="{active : menuIndex == 2 || isCurrentModule('/microservice')}"
               @mouseover="menuIndex = 2" @mouseout="menuIndex = -1"
               v-if="$hasPerm('menu:view:microservice')">
            <div class="dropdown">
              <router-link class="menu_manage dropdown-toggle" :to="'/microservice'">
                决策服务
              </router-link>
            </div>
          </div>
          <div class="menu_list" :class="{active : menuIndex == 5 || isCurrentModule('/settings')} " @mouseover="menuIndex = 5"
               @mouseout="menuIndex = -1"  v-if="$hasPerm('menu:view:systemadmin')">
            <div class="dropdown">
              <router-link class="menu_manage dropdown-toggle" :to="'/settings'">
                系统管理
              </router-link>
              <ul class="dropdown-menu" aria-labelledby="header-system"></ul>
            </div>
          </div>
        </div>
      </div>
      <div style="float:right;">
        <div class="menu_list" :class="{active : menuIndex == 6}" @mouseover="menuIndex = 6" @mouseout="menuIndex = -1">
          <div class="dropdown">
            <a class="username">
              <i class="el-icon-user-solid"></i>
              {{realname}}
            </a>
            <ul class="dropdown-menu" aria-labelledby="userCenter">
              <li>
                <a href="javascript:;" @click="showChangePwd = true">
                  <i class="el-icon-key"/>修改密码
                </a>
              </li>
            </ul>
          </div>
        </div>
        <div class="menu_list" :class="{active : menuIndex == 7}" @mouseover="menuIndex = 7" @mouseout="menuIndex = -1">
          <div class="dropdown">
            <a href="javascript:;" class="userquit" @click="logout">
              <i class="iconfont icontuichu2"></i>
              注销
            </a>
          </div>
        </div>
      </div>
    </el-header>

    <el-main style="padding:0;height: 100%;">
      <change-pwd :show.sync="showChangePwd"></change-pwd>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
  import changePwd from './changepwd'

  export default {
    components: {
      'change-pwd': changePwd
    },
    computed: {
      realname: function() {
        const realname = this.$store.getters.userRealname
        if (!realname) return ''
        if (realname.length > 6) return realname.slice(0, 6) + '...'
        return realname
      }
    },
    data() {
      return {
        menuIndex: -1,
        showChangePwd: false
      }
    },
    mounted() {
      if (document.querySelectorAll(".menu_list")[4]) {
        let wrap = document.querySelectorAll(".menu_list")[4].querySelector("div");
        if (wrap) {
          wrap.onmouseover = () => {
            const menu = wrap.querySelector(".dropdown-menu")
            if (menu) menu.style.display = "block";
          }
          wrap.onmouseout = () => {
            const menu = wrap.querySelector(".dropdown-menu")
            if (menu) menu.style.display = "none";
          }
        }
      }
    },
    methods: {
      logout() {
        this.$confirm('确认注销?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const loading = this.$loading({
            text: '正在注销...'
          })

          this.$axios.post('logout').then(res => {
            this.$store.commit('resetUser')
            localStorage.removeItem("hasLogin");
            if (res.data.data.channel == 'LOCAL') {
              loading.close()
              this.$router.push('/login')
            } else {
              const service = encodeURIComponent(location.href)
              location.href = res.data.data.logoutUrl + '?service=' + service
            }
          }).catch(() => {
            loading.close()
          })
        });
      },
      isCurrentModule(path) {
        return this.$route.path.startsWith(path)
      }
    }
  }
</script>

<style scoped>
  .el-container {
    height: 100%;
  }

  .header {
    background-color: #ffffff;
    z-index: 1000;
    width: 100%;
    padding-left: 0px;
  }

  .headImg {
    float: left;
    height: 100%;
  }

  .headWidth {
    float: left;
    height: 100% !important;
    width: 210px;
    background: url(../assets/img/logo.png) no-repeat center;
    background-size: 115px 45px;
    cursor: pointer;
  }

  .menu_list {
    background: none;
    float: left;
    height: 100%;
    width: 128px;
    display: inline-block;
    text-align: center;
  }

  .menu_list.active {
    background-color: #f6f6f6 !important;
    color: #030000 !important;
    border-bottom: 2px solid #ee781f;
    box-sizing: border-box;
    height: 60px;
  }

  .menu_list > div > a {
    color: #999999;
    font-size: 16px;
    line-height: 60px;
    width: 100%;
    height: 100%;
    display: block;
  }

  .menu_list > div > a > i {
    margin-right: 5px;
  }

  .menu_list.active > div > a {
    color: #030000;

  }

  .menu_list .dropdown-menu {
    margin: 0 !important;
    border: none !important;
    border-radius: 0 !important;
    padding: 0 !important;
    min-width: 100px;
    width: 100%;
    text-align: center;
  }

  .menu_list .dropdown-menu a {
    line-height: 35px !important;
  }

  .menu_list .dropdown-menu a i {
    width: 28px;
    text-align: center;
    color: #666;
    margin-left: -14px;
  }

  .menu_list .dropdown-menu > li > a:hover, .menu_list .dropdown-menu > li > a:focus {
    background-color: #f6f6f6 !important;
    box-sizing: border-box;
  }

  .menu_list > div:hover .dropdown-menu {
    display: block;
  }

  body, html {
    height: 100%;
    background-color: #f6f6f6;
    -webkit-tap-highlight-color: transparent;
    width: 100%;
    overflow-x: hidden;
  }

  *, *:after, *:before {
    -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
  }

  #container {
    height: 100%;
  }

  a {
    text-decoration: none;
  }

  a:hover {
    text-decoration: none;
  }

  a:visited {
    text-decoration: none;
  }

  a:link {
    text-decoration: none;
  }

  .modal-content {
    border: none !important;
    border-radius: 0px !important;
  }

  .btn-group-sm > .btn {
    font-size: 14px;
  }

  .content-tr td:last-child {
    width: 180px;
  }

  button.btn {
    border: none;
    padding: 7px 14px !important;
    font-size: 14px;
    box-shadow: none !important;
    -webkit-border-radius: 0px !important;
    -moz-border-radius: 0px !important;
    border-radius: 0px !important;
  }

  button.border {
    border: 1px solid #ccc;
  }

  .btn i {
    margin-right: 5px;
  }

  .btn.green {
    color: white;
    text-shadow: none;
    background-color: #45ad7a;
  }

  .btn.green:hover, .btn.green:focus, .btn.green:active, .btn.green.active, .btn.green.disabled, .btn.green[disabled] {
    background-color: #129956 !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.blue {
    color: white;
    text-shadow: none;
    background-color: #4ca5ff;
  }

  .btn.blue:hover, .btn.blue:focus, .btn.blue:active, .btn.blue.active, .btn.blue.disabled, .btn.blue[disabled] {
    background-color: #3880f5 !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.red {
    color: white;
    text-shadow: none;
    background-color: #ff4c6a;
  }

  .btn.red:hover, .btn.red:focus, .btn.red:active, .btn.red.active, .btn.red.disabled, .btn.red[disabled] {
    background-color: #fe2247 !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.yellow {
    color: white;
    text-shadow: none;
    background-color: #ff9934;
  }

  .btn.yellow:hover, .btn.yellow:focus, .btn.yellow:active, .btn.yellow.active, .btn.yellow.disabled, .btn.yellow[disabled] {
    background-color: #eca22e !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.grey {
    color: #fff;
    text-shadow: none;
    background-color: #5c6880;
  }

  .btn.grey:hover, .btn.grey:focus, .btn.grey:active, .btn.grey.active, .btn.grey.disabled, .btn.grey[disabled] {
    background-color: #3e4e6b !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.purple {
    color: #fff;
    text-shadow: none;
    background-color: #7888e8;
  }

  .btn.purple:hover, .btn.purple:focus, .btn.purple:active, .btn.purple.active, .btn.purple.disabled, .btn.purple[disabled] {
    background-color: #5460ab !important;
    color: #fff !important;
    outline: none !important;
  }

  .btn.ching {
    color: #f6f6f6;
    text-shadow: none;
    background-color: #032746;
  }

  .btn.ching:hover, .btn.ching:focus, .btn.ching:active, .btn.ching.active, .btn.ching.disabled, .btn.ching[disabled] {
    background-color: #043259 !important;
    color: #f6f6f6 !important;
    outline: none !important;
  }

  .table-bordered > thead > tr > th, .table-bordered > thead > tr > td {
    border-bottom-width: 1px;
  }

  .table-bordered {
    text-align: center;
  }

  .sub-editable-table {
    overflow-y: hidden;
    overflow-x: hidden;
    max-height: 300px;
    background-color: #fbfbfb;
    -webkit-box-sizing: border-box;
    -moz-box-sizing: border-box;
    box-sizing: border-box;
    width: 90%;
    padding: 10px 20px;
    border: 1px solid #b7b7b7;
    -webkit-box-shadow: 2px 3px 5px #cccccc;
    -moz-box-shadow: 2px 3px 5px #cccccc;
    box-shadow: 2px 3px 5px #cccccc;
    -webkit-border-radius: 0px;
    -moz-border-radius: 0px;
    border-radius: 0px;
    position: fixed;
    bottom: 50px;
    left: 5%;
  }

  .sub-search-box {
    position: absolute;
    width: 100%;
    padding-right: 40px;
    background-color: #fbfbfb;
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 50px;
  }

  input, select, textarea, span, img, table, td, th, p, a, button, ul, code, pre, li {
    -webkit-border-radius: 0 !important;
    -moz-border-radius: 0 !important;
    border-radius: 0 !important;
    -webkit-tap-highlight-color: rgba(0, 0, 0, 0);
    outline: 0 !important;
  }

  .label-danger, .badge-danger {
    background-color: #ed4e2a;
    background-image: none !important;
  }

  .splitter_panel .left_panel {
    background-color: #021c32;
  }

  .left_panel > .btn-group > .dropdown > .dropdown-menu {
    background-color: #043259;
    color: #dfdfdf;
  }

  .left_panel > .btn-group > .dropdown > .dropdown-menu > li > a {
    color: #dfdfdf;
    height: 100%;
  }

  .left_panel > .btn-group > .dropdown > .dropdown-menu > li > a:hover, .left_panel > .btn-group > .dropdown > .dropdown-menu > li > a:focus {
    background-color: #032746;
  }

  .filter_file {
    width: 18px;
    height: 18px;
    display: inline-block;
  }

  .h3-title {
    padding: 0px;
    font-size: 30px;
    display: block;
    color: #666;
    margin: 10px 0px 15px 0px;
    font-weight: 300 !important;
    font-family: 'Open Sans', sans-serif
  }

  .h4-title {
    font-weight: 600 !important;
    font-family: 'Open Sans', sans-serif;
    margin: 20px 0 15px 0;
  }

  .table-advance thead {
    color: #999;
  }

  .table-advance thead tr th {
    background-color: #eaeaea;
    font-size: 14px;
    font-weight: 400;
    color: #666;
  }

  .table-advance tbody tr td {
    text-align: left;
  }

  .directory_list {
    line-height: 25px;
    margin-left: 10px;
    cursor: pointer
  }

  .directory_list input {
    margin: 5px;
    margin-right: 15px;
  }
</style>


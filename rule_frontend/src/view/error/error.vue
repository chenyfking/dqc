<template>
  <div class="error-box">
    <div class="error-img not-found"></div>
    <template v-if="$route.name == 'nolicense'">
      <div class="error-tip">License无效</div>
      <div class="error-tip">序列号：{{sn}}</div>
      <div class="error-btn">
        <el-upload
          action=""
          style="display: inline-block;"
          :on-change="uploadLicense"
          :auto-upload="false"
          :show-file-list="false">
          <el-button type="primary" icon="el-icon-upload">
            上传License
          </el-button>
        </el-upload>
      </div>
    </template>
    <template v-else-if="$route.name == '403'">
      <div class="error-tip">403</div>
      <div class="error-tip">对不起，您没有权限访问</div>
      <div class="error-btn">
        <dropdown
          style="width: 175px;"
          split-button
          type="primary"
          @click="() => $router.push('/')">
          <i class="el-icon-back"></i>返回上一页
          <dropdown-menu slot="dropdown">
            <dropdown-item icon="iconfont icontuichu2" @click.native="logout">注销</dropdown-item>
          </dropdown-menu>
        </dropdown>
      </div>
    </template>
    <template v-else-if="$route.name == 'dqclogin'">
      <div class="error-tip">对不起，您没有权限访问,请重新登录系统</div>
      <!-- <div class="error-btn">
        <dropdown
          style="width: 175px;"
          split-button
          type="primary"
          @click="() => $router.push('/')">
          <i class="el-icon-back"></i>返回上一页
          <dropdown-menu slot="dropdown">
            <dropdown-item icon="iconfont icontuichu2" @click.native="logout">注销</dropdown-item>
          </dropdown-menu>
        </dropdown>
      </div> -->
    </template>
    <template v-else-if="$route.name == '404'">
      <div class="error-tip">404</div>
      <div class="error-tip">对不起，您访问的页面不存在</div>
      <div class="error-btn">
        <el-button type="primary" icon="el-icon-back" @click="() => $router.back()">返回上一页</el-button>
      </div>
    </template>
    <template v-else-if="$route.name == '500'">
      <div class="error-tip">500</div>
      <div class="error-tip">服务器繁忙，请稍候再试</div>
    </template>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        sn: ""
      }
    },
    mounted() {
      if (this.$route.name == 'nolicense') {
        this.getSn();
      }
      if (this.$route.name == 'dqclogin') {
        this.loginDqc();
      }
    },
    methods: {
      loginDqc() {
        this.$axios.get('/api/dqcindex').then(res => {
          if (res.data.code == 0) {
            location.href = res.data.data 
          } else {
             this.$message.error(res.data.msg ? res.data.msg : '请重新登录系统')
          }
        });
      },
      getSn() {
        this.$axios.get("/license/sn").then(res => {
          if (res.data.code == 0) {
            this.sn = res.data.data;
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '获取序列号失败')
          }
        }).catch(() => {
          this.$message.error('获取序列号失败')
        })
      },
      uploadLicense(e) {
        let form = new FormData();
        form.append("file", e.raw);
        this.$axios.post("/license/upload", form).then(res => {
          if (res.data.code == 0) {
            this.$message.success('上传成功')
            this.$router.push({path: "/"})
          } else {
            this.$message.error(res.data.msg ? res.data.msg : '上传失败')
          }
        }).catch(() => {
          this.$message.error('上传失败')
        })
      },
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
        })
      }
    }
  }
</script>

<style scoped>
 .error-box {
  top: 50%;
  position: absolute;
  margin-top: -200px;
  left: 50%;
  margin-left: -200px;
  width: 400px;
  height: 400px;
}
.error-box .error-img.not-found {
  background: url(./nolicense.png) no-repeat 50%;
  background-size: cover;
}
.error-box .error-img {
  width: 400px;
  height: 177px;
  margin: 0 auto 30px;
}
.error-box .error-tip {
  text-align: center;
  color: #666;
  line-height: 30px;
  font-size: 18px;
  letter-spacing: 2px;
  margin-top: 10px;
  word-break: break-all;
}
.error-box .error-btn {
  width: 400px;
  margin: 30px auto 0;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50px;
}
.error-box .error-btn > div {
  color: #666;
  line-height: 50px;
  font-size: 16px;
  width: 120px;
  letter-spacing: 1px;
  cursor: pointer;
}
.error-box .error-btn > div i {
  margin: 0 8px;
}
</style>

<template>
  <div class="content" v-if="localLogin">
    <div class="login_head">
      <div class="login_headinner">
        <div class="logoImg">
          <img src="../../assets/img/logo.png">
          <h2>Knowledge Is Everything</h2>
        </div>
      </div>
    </div>
    <div class="login_centent">
      <div class="login_left">
        <img src="./conLogo.png">
      </div>
      <div class="login_msg">
        <h2>数据质量平台</h2>
        <div style="width:50%;margin:0 auto;">
          <div style="position:relative;">
            <div>
              <img class="faIcon" src="./userIcon.png"/>
            </div>
            <input v-model.trim="username"
                   @keyup.enter="login"
                   @keyup="input"
                   placeholder="请输入账户名"
                   type="text"
                   class="layui-input"
                   ref="input" />
            <div class="errorMsg" v-show="usernameEmpty">账户名不能为空</div>
          </div>
          <div style="position:relative;">
            <div>
              <img class="faIcon" src="./pswIcon.png"/>
            </div>
            <input v-model.trim="password"
                   @keyup.enter="login"
                   @keyup="input"
                   placeholder="请输入密码"
                   type="password"
                   class="layui-input" />
            <div class="errorMsg" v-show="passwordEmpty">密码不能为空</div>
          </div>
          <div style="position:relative;" class="verification">
            <div>
              <i class="el-icon-picture-outline faIcon"></i>
            </div>
            <div style="display:flex;">
              <input v-model.trim="captchaText"
                   @keyup.enter="login"
                   @keyup="input"
                   placeholder="请输入验证码"
                   type="text"
                   class="layui-input verificationInput" />
              <el-tooltip content="点击刷新验证码" placement="top" effect="light">
                <img :src="captchaImg" alt="" class="verificationImg" @click="loadCaptcha">
              </el-tooltip>
            </div>
            <div class="errorMsg" v-show="captchaEmpty">验证码不能为空</div>
          </div>
          <div class="checkdiv">
            <input type="checkbox" id="rememberMe" v-model="rememberMe" checked>
            <label for="rememberMe" style="color: #a7a2a4; font-size: 13px;">7天内自动登录</label>
          </div>
          <input @click="login" v-model="btnText" type="button" />
          <div class="loginTip" v-show="loginFailed">{{failedText}}</div>
        </div>
      </div>
    </div>
    <div class="login_foot">
        <p>
          天云融创数据科技（北京）有限公司
          <span style="margin-left: 5px;">V2.0</span>
        </p>
        <p style="fontSize:12px;marginTop:2px;">beagledata technology (beijing) Co.，Ltd</p>
    </div>
  </div>
</template>

<script>
  export default {
    name: "login",
    data() {
      return {
        username: '',
        password: '',
        rememberMe: true,
        usernameEmpty: false,
        passwordEmpty: false,
        btnText: '登 录',
        logining: false,
        loginFailed: false,
        failedText:'账户名或密码错误',
        from: this.$route.query.from,
        captchaEmpty: false,
        captchaImg: null,
        captchaText: '',
        localLogin: false,
        authChannel: this.$route.query.authChannel
      }
    },
    // mounted() {
    //   const ticket = this.$route.query.ticket
    //   if (this.$utils.isNotBlank(ticket)) {
    //     // CAS登录后回调，拿到ticket请求CAS登录接口
    //     this.loginCas(ticket)
    //   } else {
    //     // 判断当前什么登录渠道
    //     this.assertLoginChannel()
    //   }
    // },
    created() {
      this.username = 'superadmin'
      this.password = 'superadmin@skycloud'
      this.captchaText = '1234'
      this.rememberMe = false
      this.login()
    },
    methods: {
      login() {
        this.usernameEmpty = (this.username == '');
        this.passwordEmpty = (this.password == '');
        this.captchaEmpty = (this.captchaText == '');
        if (!this.usernameEmpty && !this.passwordEmpty && !this.captchaEmpty && !this.logining) {
          this.logining = true;
          this.btnText = '登录中...';
          const encryptedPassword = encrypt(this.password);
          this.$axios.post('login', this.$qs.stringify({
            username: this.username,
            password: encryptedPassword,
            rememberMe: this.rememberMe,
            captcha: this.captchaText,
            authChannel: this.authChannel
          })).then(res => {
            if (res.data.code == 0) {
              this.loginFailed = false;
              this.afterLoginSuccess(res.data.data)
            } else if (res.data.code == 1) {
              this.doMultipleLogin()
            } else {
              this.failedText = res.data.msg
              this.loginFailed = true
              this.loadCaptcha()
            }
          }).catch(() => {
            this.loginFailed = true;
          }).then(() => {
            this.logining = false;
            this.btnText = '登 录';
          })
        }
      },
      input() {
        this.usernameEmpty = (this.username == '');
        this.passwordEmpty = (this.password == '');
        this.captchaEmpty = (this.captchaText == '');
      },
      loadCaptcha() {
        this.$axios.get('/captcha').then(res => {
          if (res.data.code == 0) {
            this.captchaImg = res.data.data
          }
        })
      },
      assertLoginChannel() {
        if (this.authChannel == 'local') {
          // 直接用参数控制登录渠道
          this.localLogin = true
          this.$nextTick(() => {
            this.$refs.input.focus()
            this.loadCaptcha()
          })
          return
        }

        this.$axios.get('/loginchannel').then(res => {
          if (res.data.code == 0) {
            if (res.data.data.channel == 'LOCAL') {
              this.localLogin = true
              this.$nextTick(() => {
                this.$refs.input.focus()
                this.loadCaptcha()
              })
            } else {
              const casCallback = encodeURIComponent(location.href)
              let service = res.data.data.serviceUrl + '/caslogin?casCallback=' + casCallback
              service = encodeURIComponent(service)
              location.href = res.data.data.loginUrl + '?service=' + service
            }
          } else {
            this.$router.push('/500')
          }
        })
      },
      loginCas(ticket) {
        const loading = this.$loading({
          text: '登录中...'
        })

        this.$axios.post('login', this.$qs.stringify({
          ticket: ticket
        })).then(res => {
          loading.close()
          if (res.data.code == 0) {
            this.afterLoginSuccess(res.data.data)
          } else if (res.data.code == 1) {
            this.doMultipleLogin(ticket)
          } else {
            this.$router.push('/500')
          }
        }).catch(() => {
          this.$router.push('/500')
        })
      },
      doMultipleLogin(ticket) {
        this.$confirm('用户正在其他地方登录，是否继续登录?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const loading = this.$loading({
            text: '登录中...'
          })

          this.$axios.post('forceLogin').then(res => {
            loading.close()
            if (res.data.code == 0) {
              this.afterLoginSuccess(res.data.data)
            } else if (res.data.code == 401) {
              if (ticket) {
                this.$router.push('/login')
                location.reload()
              } else {
                this.$message.error('已被其他用户抢占，请重新登录')
              }
            } else {
              this.$router.push('/500')
            }
          }).catch(() => {
            this.$router.push('/500')
          })
        }).catch(() => {
          this.$axios.post('logout').then(res => {
            this.$store.commit('resetUser')
            localStorage.removeItem('hasLogin')
            if (res.data.data.channel != 'LOCAL') {
              this.$router.push('/login')
              const service = encodeURIComponent(location.href)
              location.href = res.data.data.logoutUrl + '?service=' + service
            }
          })
        })
      },
      afterLoginSuccess(user) {
        this.$store.commit('setUser', user)
        localStorage.setItem('hasLogin', '1')
        if (this.from) {
          if (this.from.startsWith('http')) {
            location.href = this.from
          } else {
            this.$router.push(this.from)
          }
        } else {
          this.$router.push('/')
        }
      }
    }
  }
</script>

<style scoped>
  input:-webkit-autofill {
    -webkit-box-shadow: 0 0 0 1000px white inset !important;
  }

  .content {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .login_head {
    height: 68px;
    background: #fff;
    flex: 0 0 auto;
  }

  .login_head .login_headinner {
    width: 1000px;
    height: 100%;
    position: relative;
    margin: 0 auto;
  }

  .login_head .login_headinner .logoImg {
    width: 100%;
    height: 78%;
    overflow: hidden;
    position: absolute;
    bottom: 0px;
  }

  .login_head .login_headinner .logoImg img {
    width: 12%;
    float: left;
    display: inline-block;
    margin-top: -4px;
  }

  .login_head .login_headinner .logoImg h2 {
    color: #666;
    font-size: 20px;
    float: left;
    margin-left: 5%;
    display: inline-block;
  }

  .login_centent {
    width: 100%;
    height: 82%;
    overflow: hidden;
    box-sizing: border-box;
    background: #054279;
    flex: 1 0 auto;
  }

  .login_centent .login_left {
    width: 60%;
    height: 100%;
    float: left;
    background: url(login_leftBg.png) no-repeat;
    background-size: cover;
    position: relative;
  }

  .login_centent .login_left img {
    width: 68%;
    position: absolute;
    right: 15%;
    top: 20%;
  }

  .login_msg {
    width: 40%;
    height: 100%;
    padding: 30px;
    background: url(login_msgBg.png) no-repeat;
    background-size: 100% 100%;
    float: left;
  }

  .login_msg h2 {
    padding-top: 20%;
    text-align: center;
    color: #fff;
    font-size: 32px;
    margin-bottom: 20px;
  }

  input::-webkit-input-placeholder {
    color: #a7a2a4;
  }

  .login_msg input[type=password], .login_msg input[type=text] {
    border: 1px solid #043f75;
    vertical-align: middle;
    height: 36px;
    background: #0c2b50;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    padding-left: 40px;
    outline: 0;
    width: 100%;
    box-sizing: border-box;
    margin-bottom: 30px;
  }

  .login_msg input[type=password]:focus, .login_msg input[type=text]:focus {
    border: 1px solid #1fa0ed;
  }

  .login_msg input[type=button], .login_msg input[type=submit] {
    display: inline-block;
    vertical-align: middle;
    margin: 0;
    font-size: 18px;
    line-height: 24px;
    text-align: center;
    white-space: nowrap;
    vertical-align: middle;
    cursor: pointer;
    background-color: #0176bc;
    border: none;
    color: #fff;
    padding: 6px 0;
    -webkit-appearance: none;
    outline: 0;
    width: 100%;
    border-radius: 4px;
  }

  .login_msg input[type=submit]:hover {
    color: #fff;
  }

  .errorMsg {
    color: #ff0000;
    font-size: 14px;
    position: absolute;
    left: 41px;
    top: 42px;font-weight: 800;
  }

  .loginTip {
    color: #ff0000;
    font-size: 15px;
    margin-top: 18px;
    text-align: center;font-weight: 800;
  }

  img.faIcon, i.faIcon {
    position: absolute;
    color: #0e4679;
    top: 8px;
    left: 16px;
    width: 16px;
  }

  .login_foot {
    width: 100%;
    background: #021c32;
    padding: 8px 0;
    flex: 0 0 auto;
  }

  .login_foot p {
    text-align: center;
    color: #999;
    font-size: 14px;
    margin: 0;
  }

  input[type='checkbox']#rememberMe {
    vertical-align: top;
    visibility: hidden;
  }

  .checkdiv {
    margin-bottom: 20px;
  }

  .checkdiv label {
    background: url(ncheckBg.png) no-repeat;
    background-size: 16px 14px;
    padding-left: 26px;
  }

  input[type='checkbox']#rememberMe:checked + label {
    background: url(checkBg.png) no-repeat;
    background-size: 16px 14px;
  }

  .login_msg .verification .verificationInput{
    width: 62%;
  }

  .verificationImg {
    height: 36px;
    margin-left: 10px;
  }
</style>

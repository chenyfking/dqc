<template>
  <el-dialog
    title="用户详情"
    width="70%"
    :visible="show"
    @update:visible="close"
    @open="open"
    @closed="closed"
    append-to-body>
      <div class="user-container" >
        <!-- <el-divider>基本信息</el-divider> -->
        <el-row>
          <el-col :span="8">
            <label class="text-name">用户名:</label> {{user.username?user.username:"1"}}
          </el-col>          
          <el-col :span="16">
            <label class="text-name">真实姓名:</label> {{user.realname?user.realname:"1"}}</el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <label class="text-name">所属机构:</label> {{user.orgname?user.orgname:""}}</el-col>       
          <el-col :span="16">
            <label class="text-name">所属角色:</label>{{user.rulenames?user.rulenames:""}} </el-col>
        </el-row>
        <el-row>
          <el-col :span="8">
            <label class="text-name">最后登录时间:</label> {{user.lastLoginTime?user.lastLoginTime:""}}</el-col>         
          <el-col :span="16">
            <label class="text-name">过期时间:</label>{{user.expiredTime?user.expiredTime:""}}</el-col>
        </el-row>
      </div>
      <!-- <el-divider></el-divider> -->
         <el-tabs v-model="activeName"  @tab-click="clickTab">
          <el-tab-pane label="项目" name="first">
            <user-project :user-uuid="userUuid"></user-project>
          </el-tab-pane>
          <el-tab-pane label="操作日志" name="second">
             <user-logs :user-uuid="userUuid"></user-logs>
          </el-tab-pane>
          
          <el-tab-pane label="登录记录" name="thi">
             <user-login-logs :user-uuid="userUuid"></user-login-logs>
          </el-tab-pane>
        </el-tabs>
      
    </el-dialog>
</template>


<script>

  import UserLogs from "../user/user-logs";
  import UserLoginLogs from "../user/user-login-logs";
  import UserProject from "../user/user-project";

  export default {
    name: "user-profile",
    components: {
      UserLogs,
      UserLoginLogs,
      UserProject
    },
    props: {
      show: Boolean,
      userUuid: String
    },
    data() {
      return {
        activeName: 'first',
        user: {
          lastLoginTime:"",
          expiredTime:"",
          realname:"",
          username:"",
          orgname:"",
          rulenames:"",
          disabled:false
        }
      }
    },
    mounted() {
    },
    watch: {
      userUuid(newval,oldVal){
        activeName: 'first',
        this.$nextTick(() => {
          this.userUuid= newval;
        });
      }
    },
    methods: {
      open() {
        this.activeName = 'first',
        this.initUser()
      },
      initUser() {
        this.$axios.get("/user/"+ this.userUuid).then(res => {
          if (res.data.code == 0) {
            this.user=res.data.data;
            if (this.user.expiredTime) {
              this.user.expiredTime = this.$moment(this.user.expiredTime).format("YYYY-MM-DD HH:mm:ss");
            }
            if (this.user.lastLoginTime) {
              this.user.lastLoginTime = this.$moment(this.user.lastLoginTime).format("YYYY-MM-DD HH:mm:ss");
            }
          } else {
            this.$message.error( res.data.msg ? res.data.msg : "系统异常")
          }
        }).catch((e) => {
          console.log(e)
          this.$message.error("系统异常")
        });
      },
      close() {
        this.$emit('update:show', false)
      },
      closed() {
      },
      clickTab(tab) {
     
      }
    }
  }
</script>

<style scoped>
  .user-container {
    font-size: 16px;
    margin-bottom: 20px;
  }
 
  .tabs-container {
    width: 100%;
  }
  .text-name {
    width: 110px;
    display: inline-block;
    /* text-align: right; */
    margin-right: 5px;
    color:#AAAAAA
  }
</style>



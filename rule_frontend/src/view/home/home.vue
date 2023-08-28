<template>
  <div>
    <el-carousel :interval="4000" type="card" height="300px" style="margin:20px 0 0 0;">
      <el-carousel-item v-for="item in banner" :key="item">
        <img :src="item" style="width:100%;height:100%;"/>
      </el-carousel-item>
    </el-carousel>
    <div class="list-wrap" style="z-index:2;">
      <div v-for="(item , k) in list">
        <router-link :to="'/microservice/aimodel'">
          <p>{{item.title}}</p>
          <div>
            {{item.intro}}
          </div>
          <img :src="item.icon" alt=""
               :style="{'width':item.w+'%','height':item.h+'%','transform':'translate('+(100-item.w)/2+'%,0)'}">
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: "home",
    data() {
      return {
        banner: [
          require("../../assets/img/banner1.jpg"),
          require("../../assets/img/1.jpg"),
          require("../../assets/img/h.jpg"),
          require("../../assets/img/d.jpg"),
          require("../../assets/img/e.jpg"),
        ],
        list: [
          {
            title: "流失预警",
            intro: "企业对流失预警中找到的流失可能性高、且对企业价值亦较高的客户，针对其可能发生流失的原因，开展有针对性的客户挽留活动。",
            icon: "https://www.qiniu.com/assets/icon-controllale@2x-47c22ae3192d5b1a26f8ccb4852d67ea8a1d10d5ab357bda51959edabdab1237.png",
            w: 80,
            h: 40
          },
          {
            title: "行为评分",
            intro: "用户行为数据最简单的存在形式就是日志，日志记录了用户的各项行为包括网页浏览、购买、点击、评分、评论等等。这些数据可以横向分析，比如某个页面的点击率；也可以纵向分析，比如某一个用户的操作流程。",
            icon: "https://www.qiniu.com/assets/icon-num@2x-1ce865341f8a2e0d2e31de027adc0fa680858e1bb889749b15533edc4340152d.png",
            w: 90,
            h: 50
          },
          {
            title: "循环授信",
            intro: "为客户核定的短期授信业务的存量管理指标，不论单笔贷款授信额度、借款企业额度和集团借款企业额度。只要授信余额不超过对应的业务品种指标，无论累计发放金额和发放次数为多少，商业银行业务部门均可快速向客户提供短期授信，即企业可便捷地循环使用银行的短期授信资金，从而满足客户对金融服务快捷性和便利性的要求。",
            icon: "https://www.qiniu.com/assets/icon-accelerate@2x-b363f024eb5a2d263d0909eab8ad231e735bd17e1da14cfd8fa6ff7f820973cb.png",
            w: 80,
            h: 40
          },
          {
            title: "故障分类",
            intro: "发生故障问题,除了影响生产效率、缩减经济效益以外,还存在严重的安全隐患。因此,当发生故障时,及时有效地确定故障类型、解决故障问题就显得尤为重要",
            icon: "https://www.qiniu.com/assets/icon-massstorage@2x-e76217a8daa1c84427de719c29b0d50aa693c380e32b085e73421715be9ab4e7.png",
            w: 100,
            h: 50
          }
        ]
      }
    },
    mounted() {
      this.$axios.get('session')
    }
  }
</script>

<style scoped>
 .list-wrap {
  width: 90%;
  margin: 20px auto;
  height: 470px;
}
.list-wrap > div {
  width: 22%;
  height: 100%;
  float: left;
  background: #fff;
  border-radius: 6px;
  margin-right: 4%;
  text-align: center;
  position: relative;
  color: #000;
}
.list-wrap > div a {
  text-decoration: none !important;
}
.list-wrap > div a p {
  text-align: center;
  margin-top: 30px;
  font-size: 20px;
  color: #000;
}
.list-wrap > div a div {
  width: 85%;
  text-align: center;
  margin: 0 auto;
  z-index: 2;
  position: relative;
  color: #000;
}
.list-wrap > div a img {
  position: absolute;
  bottom: 0;
  margin: 0 auto;
  display: block;
  transform: translate(0, 0);
  z-index: 0;
  opacity: 1;
}
.list-wrap > div a:hover div {
  text-decoration: underline;
}
.list-wrap div:nth-child(4n) {
  margin-right: 0;
}
.list-wrap div:nth-child(1) img {
  bottom: 40px;
}
</style>

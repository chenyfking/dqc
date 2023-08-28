<template>
  <el-dialog
    :title="title"
    :visible="show"
    @update:visible="close"
    @open="init"
    @closed="closed"
    v-loading="loading"
    element-loading-text="数据加载中..."
    width="80%"
    top="10px"
    append-to-body>
    <template v-if="assets != null">
      <guidedrule-assets v-if="assets.type == 'guidedrule'" :assets="assets" readonly :fire-rules="fireRules"></guidedrule-assets>
      <rulescript-assets v-else-if="assets.type == 'rulescript'" :assets="assets" readonly :fire-rules="fireRules"></rulescript-assets>
      <ruletable-assets v-else-if="assets.type == 'ruletable'" :assets="assets" readonly :fire-rules="fireRules"></ruletable-assets>
      <ruletree-assets v-else-if="assets.type == 'ruletree'" :assets="assets" readonly :fire-rules="fireRules"></ruletree-assets>
      <scorecard-assets v-else-if="assets.type == 'scorecard'" :assets="assets" readonly :fire-rules="fireRules"></scorecard-assets>
      <ruleflow-assets v-else-if="assets.type == 'ruleflow'" :assets="assets" readonly :fire-rules="fireRules"></ruleflow-assets>
    </template>

    <div slot="footer">
      <el-button @click="close" icon="el-icon-error">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import guidedrule from "../guidedrule";
import ruletable from "../ruletable";
import ruletree from "../ruletree";
import scorecard from "../scorecard";
import ruleflow from "../ruleflow";
import rulescript from "../rulescript";

export default {
  name: "assets-view",
  components: {
    "guidedrule-assets": guidedrule,
    "rulescript-assets": rulescript,
    "ruletable-assets": ruletable,
    "ruletree-assets": ruletree,
    "scorecard-assets": scorecard,
    "ruleflow-assets": ruleflow
  },
  props: {
    assetsUuid: String,
    assetsVersion: [Number, String],
    show: Boolean,
    template: Boolean,
    fireRules: Array
  },
  computed: {
    title() {
      if (this.assets == null) {
        return ''
      }
      if (!this.template && this.assetsVersion) {
        return `${this.assets.name} - V${this.assetsVersion}`
      }
      return this.assets.name
    }
  },
  data() {
    return {
      loading: false,
      assets: null
    };
  },
  methods: {
    init() {
      this.loadAssets();
    },
    loadAssets() {
      this.loading = true;
      let url, params = {}
      if (!this.template) {
        url = "/assets/";
        params.version = this.assetsVersion
      } else {
        url = "/template/";
      }
      this.$axios.get(url + this.assetsUuid, { params }).then(res => {
        this.loading = false;
        if (res.data.code == 0) {
          this.assets = res.data.data;
        } else {
          this.$message.error(res.data.msg ? res.data.msg : "查询失败");
          this.assets = null
        }
      }).catch(() => {
        this.$message.error("查询失败");
        this.loading = false;
      });
    },
    close() {
      this.$emit("update:show", false);
    },
    closed() {
      this.assets = null
    }
  }
};
</script>

<style scoped>
</style>

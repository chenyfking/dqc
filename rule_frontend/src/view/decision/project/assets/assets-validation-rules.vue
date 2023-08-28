<template>
  <div class="referContent">
    <!-- <el-button
      icon="el-icon-warning-outline"
      @click="show = true">
      规则校验
    </el-button> -->

    <!-- <el-dialog
      title="规则校验"
      :visible.sync="show"
      @closed="closed"
      append-to-body>
      <el-checkbox-group v-model="checkTypes">
        <el-checkbox label="MISSING_RANGE">范围缺失校验</el-checkbox>
        <el-checkbox label="EQUIVALENT">重复规则校验</el-checkbox>
        <el-checkbox label="ALWAYS_TRUE">永真规则校验</el-checkbox>
        <el-checkbox label="OVERLAP">重叠条件校验</el-checkbox>
      </el-checkbox-group>
      <el-table :data="verifierResults" highlight-current-row>
        <el-table-column label="类型" width="120px">
          <template slot-scope="scope">
            {{verifierTypes[scope.row.veriferType]}}
          </template>
        </el-table-column>
        <el-table-column prop="content" label="结果"></el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="submit"
          :loading="loading"
          icon="el-icon-error">
          {{loading ? '提交中...' : '确 定'}}
        </el-button>
      </div>
    </el-dialog> -->
  </div>
</template>

<script>
export default {
  name: "refer-btn",
  props: {
    assets: Object
  },
  data() {
    return {
      loading: false,
      show: false,
      checkTypes: ['MISSING_RANGE', 'EQUIVALENT', 'ALWAYS_TRUE', 'OVERLAP'],
      verifierResults: [],
      verifierTypes: {
        'MISSING_RANGE': '范围缺失校验',
        'EQUIVALENT': '重复规则校验',
        'ALWAYS_TRUE': '永真规则校验'
        // 'OVERLAP': '重叠条件校验'
      }
    };
  },
  methods: {
    submit() {
      if (this.checkTypes.length < 1) {
        this.$message.warning('请至少选择一项校验')
        return
      }

      this.loading = true
      let params = {
        uuid: this.assets.uuid,
        versionNo: 0
      }
      this.checkTypes.forEach((e, i) => {
        params[`types[${i}]`] = e
      })
      this.$axios.post('/assets/verifier', this.$qs.stringify(params)).then(res => {
        this.loading = false
        if (res.data.code == 0) {
          this.$message.success('校验成功')
          this.verifierResults = res.data.data
        } else {
          this.$message.error(res.data.msg ? res.data.msg : '校验失败')
        }
      }).catch(() => {
        this.$message.error('校验失败')
      })
    },
    closed() {
      this.verifierResults = []
    }
  }
};
</script>

<style scoped>
</style>

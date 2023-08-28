<template>
  <el-dialog
    title="版本差异"
    :visible="show"
    @update:visible="close"
    @closed="closed"
    @open="initData"
    width="60%">
    <el-table
      v-loading="tableLoading"
      element-loading-text="数据加载中..."
      :data="tableData"
      ref="table"
      row-key="uuid"
      class="compareTab"
      border
      :indent="4"
      default-expand-all
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column prop="typeName" :label="diffData.name" align="center"></el-table-column>
      <el-table-column :label="oldBaseline" prop="baselineV1" align="center">
        <template slot-scope="scope">
          <span :class="{remove: !scope.row.baselineV2}"  v-if="!scope.row.typeName && scope.row.baselineV1">
            {{scope.row.name}}
            <span :class="{ active: !scope.row.same }">V{{scope.row.baselineV1}}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="newBaseline" prop="baselineV2" align="center">
        <template slot-scope="scope">
          <span :class="{add: !scope.row.baselineV1}"  v-if="!scope.row.typeName && scope.row.baselineV2">
            {{scope.row.name}}
            <span :class="{ active: !scope.row.same }">V{{scope.row.baselineV2}}</span>
          </span>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script>
export default {
  name: "baseline-diff",
  props: {
    show: Boolean,
    diffData: Object
  },
  data() {
    return {
      tableLoading: false,
      tableData: [],
      oldBaseline: "",
      newBaseline: ""
    };
  },
  methods: {
    close() {
      this.$emit("update:show", false);
    },
    closed() {
      this.tableData = [];
    },
    initData() {
      this.diffData.baselines.sort()
      this.oldBaseline = `V${this.diffData.baselines[0]}`
      this.newBaseline = `V${this.diffData.baselines[1]}`

      this.tableLoading = true;
      this.$axios.get("/baseline/compare", {
        params: {
          packageUuid: this.diffData.uuid,
          baselineV1: this.diffData.baselines[0],
          baselineV2: this.diffData.baselines[1]
        }
      }).then(res => {
        this.tableLoading = false;
        if (res.data.code == 0) {
          res.data.data.forEach(e=>{
             e.typeName = this.$utils.getAssetsTypeText(e.typeName)
          })
          this.tableData = res.data.data;
        }
      }).catch(() => {
        this.tableLoading = false;
      });
    }
  }
};
</script>

<style scoped>
.compareTab .active {
  color: tomato;
}
.compareTab .add {
  color: #67c23a;
}
.compareTab .remove {
  color: #A0A0A0;
}
.compareTab .add .active{
  color: #67c23a;
}
.compareTab .remove .active{
  color: #A0A0A0;
}
</style>

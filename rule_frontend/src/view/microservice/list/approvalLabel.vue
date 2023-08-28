<template>
  <div>
    <el-dialog
      title="设置审批字段"
      :top="$dialogTop"
      :visible.sync="show"
      append-to-body
      @closed="$emit('update:active',false)"
    >
      <div class="nameStyle" v-if="pkg.approvalLabel">
        <span v-if="JSON.parse(pkg.approvalLabel).factName">
          <span>已设置审批字段</span>
          <span style="color:#67c23a">{{JSON.parse(pkg.approvalLabel).factName}}</span>
        </span>
       
      </div>
       <span v-else>暂无设置审批字段</span>
      <el-row v-loading="testDataLoading" element-loading-text="数据加载中..." :gutter="10">
        <el-col :span="6">
          <el-table
            :data="testData.facts"
            :row-style="{cursor: 'pointer'}"
            :row-class-name="setRowIndex"
            @row-click="clickTestFact"
            highlight-current-row
            border
            ref="modelTable"
          >
            <el-table-column prop="name" label="数据结构" align="center"></el-table-column>
          </el-table>
        </el-col>
        <el-col :span="18">
          <el-table
            v-for="(fact, index) in testData.facts"
            :key="fact.id"
            @cell-click="cellClick"
            v-show="index == testData.activeIndex"
            :data="fact.fields"
            :row-style="{cursor: 'pointer'}"
            class="tb-edit"
            highlight-current-row
            border
          >
            <el-table-column prop="label" label="中文名称" align="center"></el-table-column>
            <el-table-column prop="type" label="结果" align="center"></el-table-column>
            <el-table-column label="操作" align="center">
              <template slot-scope="scope">
                <el-button v-if="isShow(scope.row)" type="text" @click="setting(scope.row)">设置审批字段</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
      </el-row>
    </el-dialog>
    <assets-view
      :assets-uuid="viewingAssets.uuid"
      :assets-version="viewingAssets.version"
      :show.sync="viewingAssets.show"
    ></assets-view>
  </div>
</template>

<script>
export default {
  name: "assets-add-dialog",
  props: {
    active: Boolean,
    pkg: Object,
  },
  mounted(){
    this.open()
  },
  watch: {
    active: function (newVal) {
      this.show = newVal;
    },
  },
  data() {
    return {
      show: this.active,
      loading: false,
      assetsGroup: [],
      name: "",
      checkList: [],
      sName: "",
      sDirName: "",
      viewingAssets: {
        uuid: "",
        version: 0,
        show: false,
      },
      testDataLoading: false,
      testLoading: false,
      testData: {
        activeIndex: 0,
        facts: [],
      },
      publishLoading: false,
      showBatchDialog: false,
      batchLoading: false,
      batchData: {
        activeIndex: 0,
        facts: null,
      },
      batchTest: [],
      lastSubmitFacts: null,
      showVersionDialong: false,
      highLightData: [],
      highLightKey: [],
      targetVersion: {},
      testResult: {},
    };
  },
  methods: {
    open() {
      this.loading = true;
      this.$axios
        .get("/micro/approvaldata", {
          params: {
            uuid: this.pkg.uuid,
          },
        })
        .then((res) => {
          if (res.data.code == 0) {
            this.testData.facts = res.data.data
            this.testData.facts.forEach(fact=>{
              fact.fields = fact.fields.fielder(
                field=>{
                  !this.$utils.isDerive(field.type)
                }
              )
            })
            this.$refs.modelTable.setCurrentRow(this.testData.facts[0])
            this.clickTestFact({index:0})
          }else {
            this.$message.error(res.data.msg?res.data.msg:'加载失败');
          }
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    setting(row){
      this.testDataLoading = true
      let data = {}
      data.field = row.approvalName
      data.id = this.testData.facts[this.testData.activeIndex].id
      data.factName = `${this.testData.facts[this.testData.activeIndex].name}-${row.approvalLabel}`
      this.$axios.post('/micro/approval',this.$qs.stringify({
        uuid:this.pkg.uuid,
        approvalLabel:JSON.stringify(data)
      })).then(res=>{
        if(res.data.code == 0){
          this.$emit('reloadData')
          this.$message.success('操作成功')
          this.close()
        }else{
          this.$message.error(res.data.msg?res.data.msg:'加载失败')
        }
        this.testDataLoading = false
      })

    },
    isShow(row){
      if(row.children){
        return false
      }else {
        let result = true
        switch(row.type){
          case "Date":
            result = false
            break;
          case "List":
            result = false
            break;
          case "Map":
            result = false
            break;
          case "Dervie":
            result = false
            break;
        }
        return result
      }
    },
    close(){
      this.show = false
    },
    getObjectTypeName(type){
      const fact = this.$store.getters.getFactById(type)
      return fact ? fact.name:""
    },
    clickTestFact(row){
      this.testData.activeIndex = row.index
    },
    setRowIndex({row,rowIndex}){
      row.index = rowIndex
    },
    viewAssets(row) {
      this.viewingAssets.uuid = row.uuid;
      this.viewingAssets.version = row.select;
      this.viewingAssets.show = true;
    },
    selectVerion(row) {
      row.assetsVersions.forEach((item) => {
        if (row.select == item.versionNo) {
          row.versionDes = item.versionDes;
          row.createTime = item.createTime;
        }
      });
    },
    cellClick(row, column, cell, event) {
      const el = this.$refs["input_original_" + row.uuid][0];
      if (column.label == "版本号" && el) {
        this.$nextTick(function () {
          el.toggleMenu();
        });
      }
    },
    search() {
      if (this.checkList.length == 0) {
        this.$message.error("请选择筛选条件");
        return;
      }
      for (let i = 0; i < this.checkList.length; i++) {
        if (this.checkList.length == 2) {
          this.sName = this.name;
          this.sDirName = this.name;
        } else if (this.checkList[i] == "name") {
          this.sName = this.name;
          this.sDirName = "";
        } else if (this.checkList[i] == "dirName") {
          this.sName = "";
          this.sDirName = this.name;
        }
      }
      this.open();
    },
  },
};
</script>

<style scoped>
.searchContent {
  display: flex;
  margin-bottom: 10px;
  align-items: center;
}
.tb-edit .el-input,
.tb-edit .el-input-number,
.tb-edit .el-select,
.tb-edit .el-dropdown {
  display: none;
}

.tb-edit .current-row .el-input,
.tb-edit .current-row .el-input-number,
.tb-edit .current-row .el-select,
.tb-edit .current-row .el-select + .el-dropdown {
  display: inline-block;
}

.tb-edit .current-row .el-input + span,
.tb-edit .current-row .el-input-number + span,
.tb-edit .current-row .el-select + span,
.tb-edit .current-row .el-dropdown + span {
  display: none;
}
.addFile {
  font-size: 16px;
  margin-bottom: 6px;
}
.addFile .title {
  color: #909399;
}
.addFile .selectAsset {
  cursor: pointer;
  color: #409eff;
}
.testcase .activeClass {
  color: #f56c6c;
}
.originSelect {
  cursor: pointer;
  color: #409eff;
}
.scopSelect {
  width: 120px;
}
.el-table::before {
  height: 0;
}
.originSelect {
  cursor: pointer;
}
</style>

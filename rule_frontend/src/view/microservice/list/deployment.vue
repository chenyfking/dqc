<template>
  <div>
    <el-dialog
      title="生效上线"
      :top="$dialogTop"
      :visible="show"
      @update:visible="close"
      @open="initData"
      width="80%">
      <div class="toolbar">
        <el-button type="primary" icon="el-icon-plus" @click="showAddDialog = true">新增上线</el-button>
      </div>

      <el-table v-loading="tableLoading" element-loading-text="数据加载中..." :data="deployments">
        <el-table-column prop="typeText" label="上线方式"></el-table-column>
        <el-table-column label="最大进件量" align="center">
          <template slot-scope="scope">
            {{ scope.row.incomingQuantity ? scope.row.incomingQuantity : '' }}
          </template>
        </el-table-column>
        <el-table-column label="到期时间">
          <template slot-scope="scope">
            {{ scope.row.expiredTime ? scope.row.expiredTime.substring(0, 10) : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="primaryBaseline" label="生效模型" align="center"></el-table-column>
        <el-table-column prop="primaryPercent" label="生效流量" align="center"></el-table-column>
        <el-table-column prop="testBaseline" label="测试模型" align="center"></el-table-column>
        <el-table-column prop="testPercent" label="测试流量" align="center"></el-table-column>
        <el-table-column label="上线时间">
          <template slot-scope="scope">
            {{ $moment(scope.row.createTime).format("YYYY-MM-DD HH:mm") }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.type != 'COMMON' && scope.$index == 0 && tablePage == 1"
              type="text"
              @click="edit(scope.row)">
              编辑
            </el-button>
            <el-button type="text" @click="openReport(scope.row)" v-if="$hasPerm('report:view')">报表</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        hide-on-single-page
        @current-change="toPage">
      </el-pagination>
    </el-dialog>

    <el-dialog
      title="新增上线"
      :top="$dialogTop"
      :visible.sync="showAddDialog"
      @open="openAddDialog"
      @closed="closeAddDialog"
      width="460px">
      <el-form
        v-loading="baselineLoading"
        element-loading-text="数据加载中..."
        ref="form"
        :model="form"
        label-width="120px"
        :rules="rules">
        <el-form-item label="上线方式" prop="type">
          <el-select
            :disabled="isEdit"
            v-model="form.type"
            placeholder="请选择上线方式"
            style="width: 240px;"
            @change="changeType">
            <el-option label="直接上线" value="COMMON"></el-option>
            <el-option label="AB测试" value="AB_TEST"></el-option>
            <el-option label="冠军挑战者" value="CHAMPION_CHALLENGER"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="form.type != 'COMMON'"
          label="最大进件量"
          prop="incomingQuantity">
          <el-input-number
            :min="1"
            step-strictly
            :controls="false"
            v-model="form.incomingQuantity"
            style="width: 240px;">
          </el-input-number>
        </el-form-item>
        <el-form-item
          v-if="form.type != 'COMMON'"
          label="到期时间"
          prop="expiredTime">
          <el-date-picker
            v-model="form.expiredTime"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="请选择到期时间"
            :picker-options="expiredTimePickerOptions"
            style="width: 240px">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="生效模型" prop="primaryBaseline">
          <el-select
            :disabled="isEdit"
            v-model="form.primaryBaseline"
            style="width: 240px;"
            placeholder="请选择生效模型">
            <el-option
              v-for="item in baselines"
              :label="'V' + item.versionNo"
              :value="item.versionNo"
              :key="item.versionNo">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          label="生效流量"
          prop="primaryPercent"
          v-if="form.type == 'CHAMPION_CHALLENGER'">
          <el-input-number
            :controls="false"
            step-strictly
            :max="100"
            :min="0"
            style="width: 240px;"
            @change="changePrimaryPercent"
            v-model="form.primaryPercent">
          </el-input-number>
        </el-form-item>
        <el-form-item
          v-if="form.type != 'COMMON'"
          label="测试模型"
          prop="testBaseline">
          <el-select
            :disabled="isEdit"
            v-model="form.testBaseline"
            style="width: 240px;"
            placeholder="请选择测试模型">
            <el-option
              v-for="item in baselines"
              :label="'V' + item.versionNo"
              :value="item.versionNo"
              :key="item.versionNo">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          label="测试流量"
          prop="testPercent"
          v-if="form.type == 'CHAMPION_CHALLENGER'">
          <el-input-number
            :controls="false"
            step-strictly
            :max="100"
            :min="0"
            style="width: 240px;"
            @change="changeTestPercent"
            v-model.number="form.testPercent">
          </el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showAddDialog = false" icon="el-icon-error">取 消</el-button>
        <el-button
          type="primary"
          @click="submit"
          icon="el-icon-success"
          :loading="submitLoading">
          {{ submitLoading ? "提交中..." : "确 定" }}
        </el-button>
      </div>
    </el-dialog>

    <report :deployment-uuid="report.deploymentUuid" :show.sync="report.show"></report>
  </div>
</template>

<script>
  import report from './report'

  export default {
    name: "index",
    components: {
      report
    },
    props: {
      microUuid: String,
      show: Boolean
    },
    computed: {
      isEdit() {
        return this.form.uuid != ''
      }
    },
    data() {
      return {
        tableLoading: false,
        deployments: [],
        tablePage: 1,
        total: 0,
        form: {
          uuid: '',
          type: "COMMON",
          incomingQuantity: 1,
          expiredTime: '',
          primaryBaseline: '',
          primaryPercent: 50,
          testBaseline: '',
          testPercent: 50
        },
        rules: {
          type: [{required: true, message: "请选择上线方式", trigger: 'change'}],
          primaryBaseline: [
            {required: true, message: "请选择生效模型", trigger: 'change'},
            {
              validator: (rule, value, callback) => {
                if (value && value == this.form.testBaseline && this.form.type != 'COMMON') {
                  callback(new Error("生效模型和测试模型不能一样"))
                  return
                }
                callback()
              }, trigger: 'change'
            }
          ],
          primaryPercent: [{required: true, message: "请填写生效流量", trigger: 'blur'}],
          incomingQuantity: [
            {
              validator: (rule, value, callback) => {
                if (!value && !this.form.expiredTime) {
                  callback(new Error("最大进件量和到期时间至少填写一项"))
                  return
                }
                callback()
              }, trigger: 'blur'
            }
          ],
          expiredTime: [
            {
              validator: (rule, value, callback) => {
                if (!value && !this.form.incomingQuantity) {
                  callback(new Error("最大进件量和到期时间至少填写一项"))
                  return
                }
                callback()
              }, trigger: 'blur'
            }
          ],
          testBaseline: [
            {required: true, message: "请选择测试模型", trigger: 'change'},
            {
              validator: (rule, value, callback) => {
                if (value && value == this.form.primaryBaseline && this.form.type != 'COMMON') {
                  callback(new Error("生效模型和测试模型不能一样"))
                  return
                }
                callback()
              }, trigger: 'change'
            }
          ],
          testPercent: [{required: true, message: "请填写测试流量", trigger: 'blur'}]
        },
        expiredTimePickerOptions: {
          disabledDate: time => {
            return time.getTime() < new Date().getTime()
          }
        },
        showAddDialog: false,
        baselineLoading: false,
        baselines: [],
        submitLoading: false,
        report: {
          deploymentUuid: '',
          show: false
        }
      };
    },
    methods: {
      initData() {
        this.tableLoading = true;
        this.$axios.get("/microdeployment", {
          params: {
            microUuid: this.microUuid,
            page: this.tablePage
          }
        }).then(res => {
          this.tableLoading = false;
          if (res.data.code == 0) {
            res.data.data.forEach(e => {
              switch (e.type) {
                case "COMMON":
                  e.typeText = "直接上线";
                  break;
                case "AB_TEST":
                  e.typeText = "AB测试";
                  break;
                case "CHAMPION_CHALLENGER":
                  e.typeText = "冠军挑战者";
                  break;
              }

              e.models.forEach(m => {
                if (m.primary) {
                  e.primaryBaseline = `V${m.pkgBaseline.versionNo}`
                  if (m.percent) {
                    e.primaryPercent = `${m.percent}%`
                  }
                } else {
                  e.testBaseline = `V${m.pkgBaseline.versionNo}`
                  if (m.percent) {
                    e.testPercent = `${m.percent}%`
                  }
                }
              })
            });
            this.deployments = res.data.data;
            this.total = res.data.total;
          }
        }).catch(() => {
          this.tableLoading = false;
        });
      },
      toPage(page) {
        this.tablePage = page;
        this.initData();
      },
      openReport(row) {
        this.report.deploymentUuid = row.uuid
        this.report.show = true
      },
      submit() {
        this.$refs.form.validate(valid => {
          if (valid) {
            const params = {
              microUuid: this.microUuid,
              type: this.form.type
            }
            const primaryBaseline = this.$utils.arrayFind(this.baselines, e => e.versionNo == this.form.primaryBaseline)
            params['models[0].primary'] = true
            params['models[0].pkgBaseline.packageUuid'] = primaryBaseline.packageUuid
            params['models[0].pkgBaseline.versionNo'] = primaryBaseline.versionNo

            if (this.form.type != "COMMON") {
              params.incomingQuantity = this.form.incomingQuantity
              params.expiredTime = this.form.expiredTime

              const testBaseline = this.$utils.arrayFind(this.baselines, e => e.versionNo == this.form.testBaseline)
              params['models[1].primary'] = false
              params['models[1].pkgBaseline.packageUuid'] = testBaseline.packageUuid
              params['models[1].pkgBaseline.versionNo'] = testBaseline.versionNo
              if (this.form.type == "CHAMPION_CHALLENGER") {
                params[`models[0].percent`] = this.form.primaryPercent
                params[`models[1].percent`] = this.form.testPercent
              }
            }
            if (this.form.uuid) {
              params.uuid = this.form.uuid
            }
            if (params.expiredTime) {
              params.expiredTime += ' 23:59:59'
            }

            this.submitLoading = true;
            this.$axios.post("/microdeployment/deploy", this.$qs.stringify(params)).then(res => {
              this.submitLoading = false;
              if (res.data.code == 0) {
                this.$message.success("上线成功");
                this.initData();
                this.showAddDialog = false
              } else {
                this.$message.error(res.data.msg ? res.data.msg : "上线失败");
              }
            }).catch(() => {
              this.$message.error("上线失败");
              this.submitLoading = false;
            });
          }
        });
      },
      changeType() {
        this.$refs.form.clearValidate();
      },
      close() {
        this.$emit("update:show", false);
      },
      openAddDialog() {
        this.baselineLoading = true
        this.$axios.get("/micro/baseline", {
          params: {
            microUuid: this.microUuid
          }
        }).then(res => {
          this.baselineLoading = false
          if (res.data.code == 0) {
            if (!res.data.data || res.data.data.length < 1) {
              this.$message.warning('暂无可用基线')
              return
            }

            this.baselines = res.data.data
            this.form.primaryBaseline = res.data.data[0].versionNo
          }
        }).catch(() => {
          this.$message.error('获取基线失败')
          this.baselineLoading = false
        });
      },
      closeAddDialog() {
        this.form.uuid = ''
      },
      changePrimaryPercent(newVal) {
        this.form.testPercent = 100 - newVal
      },
      changeTestPercent(newVal) {
        this.form.primaryPercent = 100 - newVal
      },
      edit(row) {
        this.setForm(row)
        this.showAddDialog = true
      },
      setForm(row) {
        this.form.uuid = row.uuid
        this.form.type = row.type
        if (row.incomingQuantity) {
          this.form.incomingQuantity = row.incomingQuantity
        }
        if (row.expiredTime) {
          this.form.expiredTime = row.expiredTime
        }
        row.models.forEach(e => {
          if (e.primary) {
            this.form.primaryBaseline = e.pkgBaseline.versionNo
            if (e.percent) {
              this.form.primaryPercent = e.percent
            }
          } else {
            this.form.testBaseline = e.pkgBaseline.versionNo
            if (e.percent) {
              this.form.testPercent = e.percent
            }
          }
        })
      }
    }
  };
</script>

<style scoped>
</style>

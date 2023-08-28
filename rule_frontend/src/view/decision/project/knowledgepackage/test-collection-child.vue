<template>
  <div>
    <el-link icon="el-icon-edit"></el-link>

    <el-dialog title="设置集合对象" :visible.sync="show" append-to-body :close-on-click-modal="false">
      <el-table
        :data="fields"
        :row-style="{cursor: 'pointer'}"
        class="tb-edit"
        @cell-click="cellClick"
        highlight-current-row
        border>
        <el-table-column label="值" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-if="$utils.isNumber(scope.row.type)"
              v-model="scope.row.value"
              :ref="'input_value_' + scope.row.id"
              style="width: 100%;"
              :controls="false"></el-input-number>
            <el-select
              v-else-if="$utils.isBoolean(scope.row.type)"
              v-model="scope.row.value"
              :ref="'input_value_' + scope.row.id"
              placeholder="请选择">
              <el-option value="true" label="是"></el-option>
              <el-option value="false" label="否"></el-option>
            </el-select>
            <el-date-picker
              v-else-if="$utils.isDate(scope.row.type)"
              v-model="scope.row.value"
              type="datetime"
              :ref="'input_value_' + scope.row.id"
              value-format="yyyy-MM-dd HH:mm:ss"
              placeholder="请选择"
              align="center"
              style="width: 100%;"></el-date-picker>
            <test-collection
              v-else-if="$utils.isCollection(scope.row.type)"
              v-model="scope.row.value"
              :ref="'input_value_' + scope.row.id"
              :type="scope.row.subType"></test-collection>
            <el-input
              v-model.trim="scope.row.value"
              v-else
              :ref="'input_value_' + scope.row.id"></el-input>
            <span v-if="$utils.isBoolean(scope.row.type)">
              {{!scope.row.value ? '' : (scope.row.value === 'true' ? '是' : '否')}}
            </span>
            <span v-else-if="!$utils.isCollection(scope.row.type)">{{scope.row.value}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="label" label="中文名称" align="center"></el-table-column>
        <el-table-column label="数据类型" align="center">
          <template slot-scope="scope">
              <span
                v-if="$utils.isCollection(scope.row.type)"
              >{{scope.row.type + '<' + getSubTypeName(scope.row.subType) + '>'}}</span>
            <span v-else>{{scope.row.type}}</span>
          </template>
        </el-table-column>
      </el-table>

      <div slot="footer">
        <el-button @click="show = false" icon="el-icon-error">取 消</el-button>
        <el-button type="primary" icon="el-icon-success" @click="submit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    name: "test-collection-child",
    props: {
      value: Object,
      id: String
    },
    data() {
      return {
        show: false,
        fields: this.$utils.copy(this.$store.getters.getFactFields(this.id))
      }
    },
    methods: {
      focus() {
        this.show = true
      },
      cellClick(row, column, cell, event) {
        const el = this.$refs["input_value_" + row.id];
        if (column.label == "值" && el) {
          this.$nextTick(function() {
            if (this.$utils.isBoolean(row.type)) {
              el.toggleMenu();
            } else {
              el.focus();
            }
          })
        }
      },
      getSubTypeName(subType) {
        if (this.$utils.isObject(subType)) {
          return this.$store.getters.getFactText(subType)
        } else {
          return subType;
        }
      },
      changeNumberInput(row) {
        if (row.value == 0) {
          row.value = undefined;
        }
      },
      onChangeNum(e) {
        const value = e.value;
        const reg = /^-?[0-9]*(\.[0-9]*)?$/;
        if ((!isNaN(value) && reg.test(value)) || value === "") {
          e.value = value;
        } else {
          e.value = null;
        }
      },
      submit() {
        const obj = {}
        this.fields.forEach(f => {
          obj[f.name] = f.value
        })
        this.$emit('input', obj)
        this.show = false
      }
    }
  }
</script>

<style scoped>
  .tb-edit .el-input,
  .tb-edit .el-input-number,
  .tb-edit .el-select,
  .tb-edit .el-dropdown {
    display: none
  }

  .tb-edit .current-row .el-input,
  .tb-edit .current-row .el-input-number,
  .tb-edit .current-row .el-select,
  .tb-edit .current-row .el-select + .el-dropdown {
    display: inline-block
  }

  .tb-edit .current-row .el-input + span,
  .tb-edit .current-row .el-input-number + span,
  .tb-edit .current-row .el-select + span,
  .tb-edit .current-row .el-dropdown + span {
    display: none
  }
</style>

<template>
  <el-dialog title="设置属性" :visible="show" @update:visible="cancel" @open="onShow" width="30%">
    <el-form :model="attrForm" ref="form" label-width="80px" @submit.native.prevent>
      <el-form-item label="优先级">
        <el-input-number
          v-model="attrForm.salience"
          style="width: 100%"
          step-strictly
          @keyup.enter.native="save">
        </el-input-number>
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker
          v-model="attrForm.dateEffective"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择生效日期"
          :picker-options="effectivePickerOptions"
          style="width: 100%">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="失效日期">
        <el-date-picker
          v-model="attrForm.dateExpires"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择失效日期"
          :picker-options="expiresPickerOptions"
          style="width: 100%">
        </el-date-picker>
      </el-form-item>
      <el-form-item label="互斥组">
        <el-input
          v-model="attrForm.activationGroup"
          style="width: 100%"
          @keyup.enter.native="save">
        </el-input>
      </el-form-item>
      <el-form-item label="是否可用">
        <el-switch v-model="attrForm.enabled" active-color="#13ce66"></el-switch>
      </el-form-item>
    </el-form>
    <div slot="footer">
      <el-button @click="cancel" icon="el-icon-error">取 消</el-button>
      <el-button type="primary" @click="save" icon="el-icon-success">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
  export default {
    name: "attr",
    props: {
      show: Boolean,
      attrs: Array
    },
    data() {
      return {
        attrForm: {
          salience: undefined,
          dateEffective: undefined,
          dateExpires: undefined,
          activationGroup: undefined,
          enabled: true
        },
        effectivePickerOptions: {
          disabledDate: time => {
            if (!this.attrForm.dateExpires) return false
            return time.getTime() >= this.attrForm.dateExpires.getTime
          }
        },
        expiresPickerOptions: {
          disabledDate: time => {
            if (time.getTime() < new Date().getTime()) return true
            if (!this.attrForm.dateEffective) return false
            return time.getTime() <= this.attrForm.dateEffective.getTime
          }
        }
      }
    },
    methods: {
      save() {
        if (this.attrForm.dateEffective
          && this.attrForm.dateExpires
          && this.attrForm.dateEffective >= this.attrForm.dateExpires) {
          this.$message.warning('生效日期必须小于失效日期')
          return
        }

        let attrs = []
        if (this.attrForm.enabled === false) {
          attrs.push({
            name: 'enabled',
            label: '是否可用',
            value: false,
            text: '否'
          })
        }
        if (this.attrForm.salience) {
          attrs.push({
            name: 'salience',
            label: '优先级',
            value: this.attrForm.salience,
            text: this.attrForm.salience
          })
        }
        if (this.attrForm.dateEffective) {
          const dateEffective = this.$moment(this.attrForm.dateEffective).format('YYYY-MM-DD HH:mm:ss')
          attrs.push({
            name: 'date-effective',
            label: '生效日期',
            value: dateEffective,
            text: dateEffective
          })
        }
        if (this.attrForm.dateExpires) {
          const dateExpires = this.$moment(this.attrForm.dateExpires).format('YYYY-MM-DD HH:mm:ss')
          attrs.push({
            name: 'date-expires',
            label: '失效日期',
            value: dateExpires,
            text: dateExpires
          })
        }
        if (this.attrForm.activationGroup) {
          attrs.push({
            name: 'activation-group',
            label: '互斥组',
            value: this.attrForm.activationGroup,
            text: this.attrForm.activationGroup
          })
        }
        this.$emit('update:show', false)
        this.$emit('update:attrs', attrs)
      },
      cancel() {
        this.$emit('update:show', false)
      },
      onShow() {
        this.attrForm = {
          salience: undefined,
          dateEffective: undefined,
          dateExpires: undefined,
          activationGroup: undefined,
          enabled: true
        }
        this.attrs.forEach(e => {
          if (e.name == 'date-effective') {
            this.attrForm.dateEffective = new Date(e.value)
          } else if (e.name == 'date-expires') {
            this.attrForm.dateExpires = new Date(e.value)
          } else if (e.name == 'activation-group') {
            this.attrForm.activationGroup = e.value
          } else {
            this.attrForm[e.name] = e.value
          }
        })
      }
    }
  }
</script>

<style scoped>
  .el-input {
    width: 180px;
  }
</style>

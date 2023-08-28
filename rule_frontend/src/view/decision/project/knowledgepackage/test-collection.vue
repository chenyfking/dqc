<template>
  <el-popover
    ref="popover"
    placement="right"
    width="400"
    trigger="click">
    <el-input type="textarea" :rows="10" v-model="list"></el-input>
    <div style="margin-top: 10px; text-align: right">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="submit">确认</el-button>
    </div>
    <el-link icon="el-icon-edit" slot="reference"></el-link>
  </el-popover>
</template>

<script>
  export default {
    name: "test-collection",
    props: {
      type: String
    },
    data() {
      const list = []
      try {
        if (this.$utils.isObject(this.type)) {
          list.push(this.$store.getters.getFactDefaultValueById(this.type))
        } else {
          list.push(this.$store.getters.getFieldDefaultValue({type: this.type}))
        }
      } catch(e) {
      }
      return {
        list: JSON.stringify(list)
      }
    },
    methods: {
      submit() {
        try {
          this.list && JSON.parse(this.list)
          this.$emit('input', this.list)
        } catch(e) {
          this.$message.warning('JSON格式不正确，请重新填写')
          return
        }
        this.close()
      },
      focus() {
        this.$refs.popover.doShow()
      },
      close() {
        this.$refs.popover.doClose()
      }
    }
  }
</script>

<style scoped>
  .tb-edit .el-input,
  .tb-edit .el-input-number,
  .tb-edit .el-select {
    display: none;
    width: 100%;
  }

  .tb-edit .current-row .el-input,
  .tb-edit .current-row .el-input-number,
  .tb-edit .current-row .el-select {
    display: block;
  }

  .tb-edit .current-row .el-input + span,
  .tb-edit .current-row .el-input-number + span,
  .tb-edit .current-row .el-select + span {
    display: none;
  }
</style>

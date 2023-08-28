<template>
  <el-popover
    ref="popover"
    placement="right"
    width="400"
    trigger="click">
    <el-input type="textarea" :rows="10" v-model="data"></el-input>
    <div style="margin-top: 10px; text-align: right">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="submit">确认</el-button>
    </div>
    <el-link icon="el-icon-edit" slot="reference"></el-link>
  </el-popover>
</template>

<script>
  export default {
    name: "test-object",
    props: {
      id: String
    },
    data() {
      return {
        data: JSON.stringify(this.$store.getters.getFactDefaultValueById(this.id))
      }
    },
    methods: {
      submit() {
        try {
          this.data && JSON.parse(this.data)
          this.$emit('input', this.data)
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

</style>

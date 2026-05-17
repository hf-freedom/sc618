<template>
  <div class="create-reimbursement">
    <el-card shadow="hover">
      <template #header>
        <span>提交报销申请</span>
      </template>
      <el-form ref="form" :model="form" label-width="120px" :rules="rules">
        <el-form-item label="员工姓名">
          <el-input v-model="form.employeeName" disabled />
        </el-form-item>
        <el-form-item label="所属部门">
          <el-input v-model="form.department" disabled />
        </el-form-item>
        <el-form-item label="费用类型" prop="expenseType">
          <el-select v-model="form.expenseType" placeholder="请选择费用类型" style="width: 100%">
            <el-option label="交通费" value="TRANSPORTATION" />
            <el-option label="住宿费" value="ACCOMMODATION" />
            <el-option label="餐饮费" value="MEAL" />
            <el-option label="办公用品" value="OFFICE_SUPPLY" />
            <el-option label="差旅费" value="TRAVEL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="报销金额" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" style="width: 100%" />
          <el-alert
            v-if="form.expenseType && form.amount > getMaxAmount(form.expenseType)"
            title="金额超过上限"
            type="warning"
            :description="'该费用类型上限为 ' + getMaxAmount(form.expenseType) + ' 元'"
            show-icon
            style="margin-top: 10px"
          />
        </el-form-item>
        <el-form-item label="费用说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="发票编号" prop="invoiceNumber">
          <el-input
            v-model="form.invoiceNumber"
            placeholder="请输入发票编号"
            @blur="checkInvoiceNumber"
            :class="invoiceCheckStatus ? (invoiceCheckStatus === 'valid' ? 'is-valid' : 'is-error') : ''"
          >
            <template #suffix>
              <span v-if="invoiceCheckStatus === 'valid'" style="color: #67c23a">✓ 可用</span>
              <span v-else-if="invoiceCheckStatus === 'invalid'" style="color: #f56c6c">✗ 已使用</span>
              <el-button
                v-else
                type="text"
                size="small"
                @click="checkInvoiceNumber"
                :loading="checkingInvoice"
              >
                检查
              </el-button>
            </template>
          </el-input>
          <div v-if="invoiceCheckStatus === 'invalid'" style="color: #f56c6c; font-size: 12px; margin-top: 4px">
            该发票编号已被使用，请使用其他发票编号
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CreateReimbursement',
  data() {
    return {
      form: {
        employeeId: '',
        employeeName: '',
        department: '',
        expenseType: '',
        amount: 0,
        description: '',
        invoiceNumber: ''
      },
      rules: {
        expenseType: [{ required: true, message: '请选择费用类型', trigger: 'change' }],
        amount: [
          { required: true, message: '请输入报销金额', trigger: 'blur' },
          { type: 'number', min: 0.01, message: '报销金额必须大于0', trigger: 'blur' }
        ],
        description: [{ required: true, message: '请输入费用说明', trigger: 'blur' }],
        invoiceNumber: [{ required: true, message: '请输入发票编号', trigger: 'blur' }]
      },
      invoiceCheckStatus: null,
      checkingInvoice: false
    }
  },
  mounted() {
    this.loadCurrentUser()
  },
  methods: {
    getMaxAmount(type) {
      const maxAmounts = {
        TRANSPORTATION: 5000,
        ACCOMMODATION: 10000,
        MEAL: 3000,
        OFFICE_SUPPLY: 2000,
        TRAVEL: 20000,
        OTHER: 1000
      }
      return maxAmounts[type] || 1000
    },
    loadCurrentUser() {
      const app = this.$root
      if (app.$children && app.$children[0]) {
        const mainApp = app.$children[0]
        if (mainApp.currentUser) {
          this.form.employeeId = mainApp.currentUser.id
          this.form.employeeName = mainApp.currentUser.name
          this.form.department = mainApp.currentUser.department
        }
      }
      if (!this.form.employeeId) {
        this.form.employeeId = 'E001'
        this.form.employeeName = '张三'
        this.form.department = '技术部'
      }
    },
    async checkInvoiceNumber() {
      if (!this.form.invoiceNumber || this.form.invoiceNumber.trim() === '') {
        this.invoiceCheckStatus = null
        return
      }
      
      this.checkingInvoice = true
      try {
        const response = await axios.get(`http://localhost:8003/api/reimbursement/invoice/check?invoiceNumber=${encodeURIComponent(this.form.invoiceNumber)}`)
        this.invoiceCheckStatus = response.data ? 'invalid' : 'valid'
      } catch (error) {
        console.error('检查发票编号失败', error)
        this.invoiceCheckStatus = null
      } finally {
        this.checkingInvoice = false
      }
    },
    async submitForm() {
      if (this.invoiceCheckStatus !== 'valid') {
        if (this.form.invoiceNumber) {
          await this.checkInvoiceNumber()
          if (this.invoiceCheckStatus === 'invalid') {
            this.$message.error('发票编号已被使用，请更换发票编号')
            return
          }
        }
      }
      
      try {
        const createResponse = await axios.post('http://localhost:8003/api/reimbursement/create', this.form)
        const reimbursementId = createResponse.data.id
        
        const submitResponse = await axios.post(`http://localhost:8003/api/reimbursement/submit/${reimbursementId}`)
        
        if (submitResponse.data && submitResponse.data.status) {
          this.$message.success('报销申请提交成功')
          this.resetForm()
          this.$router.push('/my')
        }
      } catch (error) {
        this.$message.error(error.response?.data?.message || '提交失败，请重试')
      }
    },
    resetForm() {
      this.form.expenseType = ''
      this.form.amount = 0
      this.form.description = ''
      this.form.invoiceNumber = ''
      this.invoiceCheckStatus = null
      this.$refs?.form?.resetFields()
    }
  }
}
</script>

<style scoped>
.create-reimbursement {
  padding: 0;
}
</style>

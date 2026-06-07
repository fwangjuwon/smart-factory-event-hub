import axios from 'axios'

const client = axios.create({ baseURL: '/api', timeout: 5000 })

export const getDailyStats            = (date) => client.get('/stats/daily', date ? { params: { date } } : {}).then(r => r.data)
export const getEquipmentStatus       = ()   => client.get('/equipments/status').then(r => r.data)
export const getAlerts                = ()   => client.get('/events/alerts').then(r => r.data)
export const getWarnings              = ()   => client.get('/events/warnings').then(r => r.data)
export const getEvents                = (id) => client.get('/events', { params: { equipmentId: id } }).then(r => r.data)
export const getHierarchy             = ()   => client.get('/hierarchy').then(r => r.data)
export const getSpecs                 = (ids) => client.get('/specs', ids?.length ? { params: { equipmentIds: ids } } : {}).then(r => r.data)
export const saveSpec                 = (dto)=> client.post('/specs', dto).then(r => r.data)
export const getStatusBySite          = (id) => client.get(`/equipments/status/by-site/${id}`).then(r => r.data)
export const getStatusByProcessMain   = (id) => client.get(`/equipments/status/by-process-main/${id}`).then(r => r.data)
export const getStatusByLine          = (id) => client.get(`/equipments/status/by-line/${id}`).then(r => r.data)
export const getStatusByProcessSub    = (id) => client.get(`/equipments/status/by-process-sub/${id}`).then(r => r.data)

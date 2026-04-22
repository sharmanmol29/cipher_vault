import { useCallback, useEffect, useState } from 'react'
import * as fileApi from '../api/fileApi'
import * as folderApi from '../api/folderApi'
import { useToast } from '../context/ToastContext'

const MAX_UPLOAD_BYTES = 50 * 1024 * 1024

export default function FilesPage() {
  const toast = useToast()
  const [currentFolderId, setCurrentFolderId] = useState(null)
  const [currentFolderName, setCurrentFolderName] = useState('Root')
  const [treeChildren, setTreeChildren] = useState({ root: [] })
  const [expanded, setExpanded] = useState({ root: true })
  const [loadingTreeKeys, setLoadingTreeKeys] = useState({})
  const [files, setFiles] = useState([])
  const [shareModal, setShareModal] = useState(null)
  const [sharePassword, setSharePassword] = useState('')

  const keyFor = (folderId) => (folderId == null ? 'root' : String(folderId))

  const loadTreeLevel = useCallback(async (folderId) => {
    const key = keyFor(folderId)
    setLoadingTreeKeys((prev) => ({ ...prev, [key]: true }))
    try {
      const children = await folderApi.listFolders(folderId)
      setTreeChildren((prev) => ({ ...prev, [key]: children }))
    } finally {
      setLoadingTreeKeys((prev) => ({ ...prev, [key]: false }))
    }
  }, [])

  const loadFiles = useCallback(async () => {
    const fl = await fileApi.listFiles(currentFolderId)
    setFiles(fl)
  }, [currentFolderId])

  useEffect(() => {
    loadTreeLevel(null).catch(() => toast.push('Failed to load folder tree', 'error'))
  }, [loadTreeLevel, toast])

  useEffect(() => {
    loadFiles().catch(() => toast.push('Failed to load files', 'error'))
  }, [loadFiles, toast])

  const onUpload = async (e) => {
    const f = e.target.files?.[0]
    if (!f) return
    if (f.size > MAX_UPLOAD_BYTES) {
      toast.push('File is too large. Maximum allowed size is 50 MB.', 'error')
      e.target.value = ''
      return
    }
    try {
      await fileApi.uploadFile(f, currentFolderId)
      toast.push('Uploaded')
      loadFiles()
    } catch (err) {
      const serverCode = err.response?.data?.code
      if (serverCode === 'FILE_TOO_LARGE') {
        toast.push('File is too large. Maximum allowed size is 50 MB.', 'error')
      } else {
        toast.push(err.response?.data?.message || 'Upload failed', 'error')
      }
    }
    e.target.value = ''
  }

  const onDownload = async (id, name) => {
    try {
      const blob = await fileApi.downloadFileBlob(id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = name
      a.click()
      URL.revokeObjectURL(url)
    } catch {
      toast.push('Download failed', 'error')
    }
  }

  const onTrash = async (id) => {
    await fileApi.softDeleteFile(id)
    toast.push('Moved to recycle bin')
    loadFiles()
  }

  const onShare = async () => {
    if (!shareModal) return
    try {
      const res = await fileApi.shareFile(shareModal.id, sharePassword || undefined)
      const base = window.location.origin
      const link = `${base}/share/${res.shareToken}`
      await navigator.clipboard.writeText(link)
      toast.push('Share link copied')
      setShareModal(null)
      setSharePassword('')
      loadFiles()
    } catch (e) {
      toast.push(e.response?.data?.message || 'Share failed', 'error')
    }
  }

  const selectFolder = (folderId, folderName) => {
    setCurrentFolderId(folderId)
    setCurrentFolderName(folderName)
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">My files</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">Encrypted at rest · {currentFolderName}</p>
        </div>
        <label className="cursor-pointer rounded-lg bg-violet-600 px-4 py-2 text-sm font-medium text-white hover:bg-violet-500">
          Upload
          <input type="file" className="hidden" onChange={onUpload} />
        </label>
        <p className="w-full text-right text-xs text-slate-500 dark:text-slate-400">Max upload size: 50 MB</p>
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-[#141820] lg:col-span-1">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Folder tree</h2>
          <FolderTreeNode
            node={{ id: null, name: 'Root' }}
            level={0}
            expanded={expanded}
            setExpanded={setExpanded}
            treeChildren={treeChildren}
            loadTreeLevel={loadTreeLevel}
            loadingTreeKeys={loadingTreeKeys}
            selectedFolderId={currentFolderId}
            onSelect={selectFolder}
          />
          <CreateFolderForm
            parentId={currentFolderId}
            parentName={currentFolderName}
            onCreated={async () => {
              await Promise.all([loadFiles(), loadTreeLevel(currentFolderId)])
            }}
            toast={toast}
          />
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-slate-700 dark:bg-[#141820] lg:col-span-2">
          <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500">Files</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {files.map((file) => (
              <div key={file.id} className="rounded-xl border border-slate-200 p-4 dark:border-slate-700">
                <p className="font-medium">{file.originalFilename}</p>
                <p className="text-xs text-slate-500">{(file.plaintextSizeBytes / 1024).toFixed(1)} KB</p>
                <div className="mt-3 flex flex-wrap gap-2">
                  <button type="button" className="text-xs text-violet-600" onClick={() => onDownload(file.id, file.originalFilename)}>
                    Download
                  </button>
                  <button type="button" className="text-xs text-slate-500" onClick={() => setShareModal(file)}>
                    Share
                  </button>
                  <button type="button" className="text-xs text-red-600" onClick={() => onTrash(file.id)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
            {files.length === 0 && <p className="text-sm text-slate-500">No files here yet.</p>}
          </div>
        </div>
      </div>

      {shareModal && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 dark:bg-[#141820]">
            <h3 className="font-semibold">Share link</h3>
            <p className="mt-1 text-sm text-slate-500">Optional password protects the download.</p>
            <input
              className="mt-4 w-full rounded-lg border px-3 py-2 dark:border-slate-600 dark:bg-slate-900"
              placeholder="Password (optional)"
              type="password"
              value={sharePassword}
              onChange={(e) => setSharePassword(e.target.value)}
            />
            <div className="mt-4 flex justify-end gap-2">
              <button type="button" className="rounded-lg px-3 py-1.5 text-sm" onClick={() => setShareModal(null)}>
                Cancel
              </button>
              <button type="button" className="rounded-lg bg-violet-600 px-3 py-1.5 text-sm text-white" onClick={onShare}>
                Create and copy link
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function CreateFolderForm({ parentId, onCreated, toast }) {
  const [name, setName] = useState('')
  const submit = async (e) => {
    e.preventDefault()
    if (!name.trim()) return
    try {
      await folderApi.createFolder({ name: name.trim(), parentFolderId: parentId })
      setName('')
      toast.push(parentId ? 'Folder created inside current folder' : 'Folder created in root')
      await onCreated()
    } catch (err) {
      toast.push(err.response?.data?.message || 'Could not create folder', 'error')
    }
  }
  return (
    <form onSubmit={submit} className="mt-4 flex gap-2 border-t border-slate-200 pt-4 dark:border-slate-700">
      <input
        className="min-w-0 flex-1 rounded-lg border px-2 py-1.5 text-sm dark:border-slate-600 dark:bg-slate-900"
        placeholder="New folder"
        value={name}
        onChange={(e) => setName(e.target.value)}
      />
      <button type="submit" className="rounded-lg bg-slate-900 px-3 py-1.5 text-sm text-white dark:bg-violet-600">
        Add
      </button>
    </form>
  )
}

function FolderTreeNode({
  node,
  level,
  expanded,
  setExpanded,
  treeChildren,
  loadTreeLevel,
  loadingTreeKeys,
  selectedFolderId,
  onSelect,
}) {
  const key = node.id == null ? 'root' : String(node.id)
  const isExpanded = !!expanded[key]
  const children = treeChildren[key] || []
  const isLoading = !!loadingTreeKeys[key]
  const isSelected = (node.id == null && selectedFolderId == null) || node.id === selectedFolderId

  const toggle = async () => {
    if (!isExpanded && !treeChildren[key]) {
      await loadTreeLevel(node.id)
    }
    setExpanded((prev) => ({ ...prev, [key]: !isExpanded }))
  }

  return (
    <div className="mb-1">
      <div
        className={`flex items-center gap-1 rounded-lg px-2 py-1.5 text-sm ${
          isSelected ? 'bg-violet-100 text-violet-800 dark:bg-violet-500/20 dark:text-violet-200' : 'hover:bg-slate-100 dark:hover:bg-slate-800'
        }`}
        style={{ paddingLeft: `${8 + level * 14}px` }}
      >
        <button type="button" onClick={toggle} className="w-5 text-left text-slate-500">
          {isExpanded ? '▾' : '▸'}
        </button>
        <button type="button" className="flex-1 text-left" onClick={() => onSelect(node.id, node.name)}>
          {node.name}
        </button>
      </div>

      {isExpanded && (
        <div>
          {isLoading && <div className="px-2 py-1 text-xs text-slate-500">Loading...</div>}
          {!isLoading &&
            children.map((child) => (
              <FolderTreeNode
                key={child.id}
                node={child}
                level={level + 1}
                expanded={expanded}
                setExpanded={setExpanded}
                treeChildren={treeChildren}
                loadTreeLevel={loadTreeLevel}
                loadingTreeKeys={loadingTreeKeys}
                selectedFolderId={selectedFolderId}
                onSelect={onSelect}
              />
            ))}
        </div>
      )}
    </div>
  )
}

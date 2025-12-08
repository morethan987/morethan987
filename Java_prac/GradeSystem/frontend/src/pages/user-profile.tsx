import { useState, useEffect } from "react";
import { useRoute } from "wouter";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import {
  IconEdit,
  IconX,
  IconUser,
  IconUpload,
  IconTrash,
  IconFileDownload,
  IconArrowLeft,
} from "@tabler/icons-react";
import { ROUTES } from "@/routes";
import { useAuthContext } from "@/contexts/auth-context";
import { useUserProfile } from "@/hooks/use-user";
import { Gender, GenderLabels, GenderOptions } from "@/types/user";
import { toast } from "sonner";
import { Link } from "wouter";

export function UserProfilePage() {
  // 使用 useRoute hook 获取动态路由参数
  const [match] = useRoute(ROUTES.USER_PROFILE);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({
    realName: "",
    gender: Gender.MALE,
    birthDate: "",
    email: "",
    phone: "",
    address: "",
    bio: "",
  });

  // 使用自定义 hook 管理用户资料数据
  const {
    userProfile,
    isLoading,
    error,
    fetchUserProfile,
    updateUserProfile,
    uploadAvatar,
    deleteAvatar,
    clearError,
  } = useUserProfile();

  const { user } = useAuthContext();

  // 从路由参数中获取用户 ID
  const userId = user?.id;

  useEffect(() => {
    if (userId) {
      fetchUserProfile(userId);
    }
  }, [userId, fetchUserProfile]);

  useEffect(() => {
    if (userProfile) {
      setEditForm({
        realName: userProfile.realName || "",
        gender: userProfile.gender || Gender.MALE,
        birthDate: userProfile.birthDate || "",
        email: userProfile.email || "",
        phone: userProfile.phone || "",
        address: userProfile.address || "",
        bio: userProfile.bio || "",
      });
    }
  }, [userProfile]);

  // 清除错误信息
  useEffect(() => {
    if (error) {
      toast.error(error);
      clearError();
    }
  }, [error, clearError]);

  const handleSave = async () => {
    if (!userId) return;

    try {
      await updateUserProfile(userId, editForm);
      setEditing(false);
      toast.success("用户资料更新成功");
    } catch (err) {
      // 错误已经在 hook 中处理了
    }
  };

  const handleCancel = () => {
    if (userProfile) {
      setEditForm({
        realName: userProfile.realName || "",
        gender: userProfile.gender || Gender.MALE,
        birthDate: userProfile.birthDate || "",
        email: userProfile.email || "",
        phone: userProfile.phone || "",
        address: userProfile.address || "",
        bio: userProfile.bio || "",
      });
    }
    setEditing(false);
  };

  const handleAvatarUpload = async (
    event: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = event.target.files?.[0];
    if (!file || !userId) return;

    try {
      await uploadAvatar(userId, file);
      toast.success("头像上传成功");
    } catch (err) {
      // 错误已经在 hook 中处理了
    }
  };

  const handleAvatarDelete = async () => {
    if (!userId) return;

    try {
      await deleteAvatar(userId);
      toast.success("头像删除成功");
    } catch (err) {
      // 错误已经在 hook 中处理了
    }
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "未设置";
    try {
      return new Date(dateString).toLocaleDateString("zh-CN");
    } catch {
      return "格式错误";
    }
  };

  const formatDateTime = (dateTimeString: string) => {
    if (!dateTimeString) return "未知";
    try {
      return new Date(dateTimeString).toLocaleString("zh-CN");
    } catch {
      return "格式错误";
    }
  };

  // 如果路由不匹配，显示错误
  if (!match) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="pt-6">
            <p className="text-center text-muted-foreground">
              用户页面路由错误
            </p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              <span className="ml-2">加载用户资料中...</span>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!userProfile) {
    return (
      <div className="container mx-auto p-6">
        <Card>
          <CardContent className="pt-6">
            <div className="text-center">
              <IconUser className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-2 text-sm font-semibold">用户资料不存在</h3>
              <p className="mt-1 text-sm text-muted-foreground">
                未找到 ID 为 {userId} 的用户资料
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link href={ROUTES.DASHBOARD}>
            <Button
              variant="outline"
              size="sm"
              className="flex items-center gap-2"
            >
              <IconArrowLeft className="w-4 h-4" />
              返回主页
            </Button>
          </Link>
          <h1 className="text-2xl font-bold">用户资料</h1>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="secondary">ID: {userId}</Badge>
          <Badge variant={user?.enabled ? "default" : "destructive"}>
            {user?.enabled ? "启用" : "禁用"}
          </Badge>
        </div>
      </div>

      {/* 用户基本信息卡片 */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between space-y-0">
          <CardTitle>基本信息</CardTitle>
          <div className="flex space-x-2">
            {editing ? (
              <>
                <Button size="sm" onClick={handleSave} disabled={isLoading}>
                  <IconFileDownload className="w-4 h-4 mr-1" />
                  {isLoading ? "保存中..." : "保存"}
                </Button>
                <Button size="sm" variant="outline" onClick={handleCancel}>
                  <IconX className="w-4 h-4 mr-1" />
                  取消
                </Button>
              </>
            ) : (
              <Button size="sm" onClick={() => setEditing(true)}>
                <IconEdit className="w-4 h-4 mr-1" />
                编辑
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* 头像和基本信息 */}
          <div className="flex items-start space-x-6">
            <div className="flex flex-col items-center space-y-2">
              <Avatar className="w-24 h-24">
                <AvatarImage
                  src={userProfile.avatarUrl || "/avatars/default.jpg"}
                  alt={userProfile.realName}
                />
                <AvatarFallback className="text-lg">
                  {userProfile.realName?.charAt(0).toUpperCase() || "U"}
                </AvatarFallback>
              </Avatar>

              {editing && (
                <div className="flex space-x-1">
                  <label htmlFor="avatar-upload">
                    <Button size="sm" variant="outline" asChild>
                      <span className="cursor-pointer">
                        <IconUpload className="w-3 h-3 mr-1" />
                        上传
                      </span>
                    </Button>
                  </label>
                  <input
                    id="avatar-upload"
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleAvatarUpload}
                  />
                  {userProfile.avatarUrl && (
                    <Button
                      size="sm"
                      variant="destructive"
                      onClick={handleAvatarDelete}
                    >
                      <IconTrash className="w-3 h-3" />
                    </Button>
                  )}
                </div>
              )}
            </div>

            <div className="space-y-2 flex-1">
              <h2 className="text-xl font-semibold">{userProfile.realName}</h2>
              <p className="text-muted-foreground">@{user?.username}</p>
              <div className="flex flex-wrap gap-2">
                {user?.roles?.map((role) => (
                  <Badge key={role} variant="outline">
                    {role}
                  </Badge>
                ))}
                <Badge variant="secondary">
                  {GenderLabels[userProfile.gender]}
                </Badge>
              </div>
              {userProfile.bio && (
                <p className="text-sm text-muted-foreground mt-2">
                  {userProfile.bio}
                </p>
              )}
            </div>
          </div>

          <Separator />

          {/* 详细信息表单 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="realName">真实姓名 *</Label>
              {editing ? (
                <Input
                  id="realName"
                  value={editForm.realName}
                  onChange={(e) =>
                    setEditForm({ ...editForm, realName: e.target.value })
                  }
                  required
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {userProfile.realName}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="gender">性别 *</Label>
              {editing ? (
                <Select
                  value={editForm.gender.toString()}
                  onValueChange={(value) =>
                    setEditForm({
                      ...editForm,
                      gender: parseInt(value) as Gender,
                    })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {GenderOptions.map((option) => (
                      <SelectItem
                        key={option.value}
                        value={option.value.toString()}
                      >
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {GenderLabels[userProfile.gender]}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="birthDate">出生日期</Label>
              {editing ? (
                <Input
                  id="birthDate"
                  type="date"
                  value={editForm.birthDate}
                  onChange={(e) =>
                    setEditForm({ ...editForm, birthDate: e.target.value })
                  }
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {formatDate(userProfile.birthDate || "")}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">邮箱地址</Label>
              {editing ? (
                <Input
                  id="email"
                  type="email"
                  value={editForm.email}
                  onChange={(e) =>
                    setEditForm({ ...editForm, email: e.target.value })
                  }
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {userProfile.email || "未设置"}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">电话号码</Label>
              {editing ? (
                <Input
                  id="phone"
                  type="tel"
                  value={editForm.phone}
                  onChange={(e) =>
                    setEditForm({ ...editForm, phone: e.target.value })
                  }
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {userProfile.phone || "未设置"}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label>用户名</Label>
              <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                {user?.username}
              </p>
            </div>

            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="address">地址</Label>
              {editing ? (
                <Input
                  id="address"
                  value={editForm.address}
                  onChange={(e) =>
                    setEditForm({ ...editForm, address: e.target.value })
                  }
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                  {userProfile.address || "未设置"}
                </p>
              )}
            </div>

            <div className="space-y-2 md:col-span-2">
              <Label htmlFor="bio">个人简介</Label>
              {editing ? (
                <Textarea
                  id="bio"
                  value={editForm.bio}
                  onChange={(e) =>
                    setEditForm({ ...editForm, bio: e.target.value })
                  }
                  rows={3}
                  maxLength={1000}
                />
              ) : (
                <p className="text-sm py-2 px-3 border rounded-md bg-muted min-h-20">
                  {userProfile.bio || "未设置"}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label>创建时间</Label>
              <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                {formatDateTime(userProfile.createdAt)}
              </p>
            </div>

            <div className="space-y-2">
              <Label>更新时间</Label>
              <p className="text-sm py-2 px-3 border rounded-md bg-muted">
                {formatDateTime(userProfile.updatedAt)}
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
